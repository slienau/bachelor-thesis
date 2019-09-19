package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.models.Heartbeat;
import de.tuberlin.aot.thesis.slienau.models.NodeRedFlow;
import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.HeartbeatMonitor;
import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.HeartbeatProcessor;
import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.ResultsMonitor;
import de.tuberlin.aot.thesis.slienau.scheduler.application.AppSoftwareModule;
import de.tuberlin.aot.thesis.slienau.scheduler.application.Application;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.Infrastructure;
import de.tuberlin.aot.thesis.slienau.scheduler.interfaces.Scheduler;
import de.tuberlin.aot.thesis.slienau.scheduler.strategy.AppDeployment;
import de.tuberlin.aot.thesis.slienau.scheduler.strategy.QosScheduler;
import de.tuberlin.aot.thesis.slienau.utils.SchedulerUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class NodeRedOrchestrator {

    public static final String MQTT_BROKER = "tcp://dslbabroker.westeurope.azurecontainer.io:1883";
    private static final NodeRedFlowDatabase flowDatabase = NodeRedFlowDatabase.getInstance();
    private static NodeRedOrchestrator instance;
    private final Infrastructure infrastructure;
    private final Queue<Heartbeat> heartbeatQueue;
    private final Scheduler scheduler;
    private AppDeployment optimalDeployment;

    private NodeRedOrchestrator() {
        infrastructure = new Infrastructure();
        heartbeatQueue = new ConcurrentLinkedQueue<>();
//        Application sensorNetworkApplication = createSensorNetworkApplication();
        Application objectDetectionApplication = createObjectDetectionApplication();
        scheduler = new QosScheduler(objectDetectionApplication, infrastructure);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[NodeRedOrchestrator] Starting...");

        NodeRedOrchestrator orchestrator = NodeRedOrchestrator.getInstance();

        Thread heartbeatMonitorThread = new Thread(new HeartbeatMonitor(MQTT_BROKER, orchestrator.heartbeatQueue));
        heartbeatMonitorThread.start();

        Thread heartbeatProcessorThread = new Thread(new HeartbeatProcessor(orchestrator));
        heartbeatProcessorThread.start();

        Thread infrastructureMaintainerThread = new Thread(new InfrastructureMaintainer(orchestrator));
        infrastructureMaintainerThread.start();

        new ResultsMonitor(MQTT_BROKER).start();
    }

    private static Application createSensorNetworkApplication() {
        Application a = new Application("sensornetwork");

        a.addHardwareModule("CAMERA", "RAW_SENSOR_DATA");
        a.addSoftwareModule("data-reader", "RAW_SENSOR_DATA", "SENSOR_DATA", 1, 0.5, SchedulerUtils.calculateRequiredInstructionsForAppModule(SchedulerUtils.CPU_SCORE_RASPI_3, 54), Arrays.asList("CAMERA"));
//        a.addSoftwareModule("data-reader", "RAW_SENSOR_DATA", "SENSOR_DATA", 1, 0.5, 1, null);
        a.addSoftwareModule("data-processor", "SENSOR_DATA", "SENSOR_DATA_PROCESSED", 10, 0.5, SchedulerUtils.calculateRequiredInstructionsForAppModule(SchedulerUtils.CPU_SCORE_MBP_2018, 1000), null);
        a.addSoftwareModule("data-viewer", "SENSOR_DATA_PROCESSED", null, 1, 0.5, 1, null);

        a.addMessage("RAW_SENSOR_DATA", 1024);
        a.addMessage("SENSOR_DATA", 1024);
        a.addMessage("SENSOR_DATA_PROCESSED", 10);

        a.addLoop("sensorNetworkLoop1", 999999, Arrays.asList("data-reader", "data-processor", "data-viewer"));
        return a;
    }

    private static Application createObjectDetectionApplication() {
        Application a = new Application("od");
        a.addHardwareModule("OD-DOCKER-CONTAINER", "IMAGE_DETECTED");
        a.addSoftwareModule("webapp", null, "IMAGE_UNDETECTED", 0, 0, 0, Arrays.asList("CAMERA"));
        a.addSoftwareModule("detector", "IMAGE_UNDETECTED", "IMAGE_DETECTED", 0, 0, SchedulerUtils.calculateRequiredInstructionsForAppModule(SchedulerUtils.CPU_SCORE_DEBIAN_VM, 1000), Arrays.asList("OD-DOCKER-CONTAINER"));

        a.addMessage("IMAGE_UNDETECTED", 1383);
        a.addMessage("IMAGE_DETECTED", 142);
        a.addLoop("objectDetectionLoop1", 999999, Arrays.asList("webapp", "detector", "webapp"), true);

        return a;
    }

    public static NodeRedOrchestrator getInstance() {
        if (NodeRedOrchestrator.instance == null)
            NodeRedOrchestrator.instance = new NodeRedOrchestrator();
        return NodeRedOrchestrator.instance;
    }

    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    public void removeFogNode(String fogNodeIdToRemove) throws IOException {
        // remove all uplinks to this node
        for (FogNode fn : infrastructure.getFogNodes()) {
            fn.removeUplinkTo(fogNodeIdToRemove);
        }

        // remove node from infrastructure
        infrastructure.removeFogNode(fogNodeIdToRemove);

        this.checkForNewOptimalDeployment();
    }

    public Queue<Heartbeat> getHeartbeatQueue() {
        return heartbeatQueue;
    }

    public void checkForNewOptimalDeployment() throws IOException {
        AppDeployment d = this.scheduler.getOptimalDeployment();
        if (d == null) {
            System.out.println("[NodeRedOrchestrator] No deployment found for application!");
            optimalDeployment = null;
            return;
        }
        if (d.equals(this.optimalDeployment)) {
            System.out.println("[NodeRedOrchestrator] No new optimal deployment found!");
            return;
        }

        System.out.println("[NodeRedOrchestrator] Found new optimal deployment!");
        optimalDeployment = d;
        System.out.println(optimalDeployment.createDetailsString());

        this.deployOptimalDeployment();

    }

    private void deployOptimalDeployment() throws IOException {
        System.out.println(String.format("[NodeRedOrchestrator] Going to deploy new optimal deployment strategy %s", optimalDeployment));
        for (FogNode fn : infrastructure.getFogNodes()) {
            NodeRedFogNode fogNode = (NodeRedFogNode) fn;
            List<String> deployedFlows = fogNode.getNodeRedController().getDeployedFlowNames();

            if (!optimalDeployment.getAllInvolvedFogNodes().contains(fogNode)) {
                // Deployment does not contain fog node --> delete all flows
                fogNode.getNodeRedController().deleteAllFlows();
                continue;
            }

            List<String> modulesToDeployOnNode = optimalDeployment.getNodeToModulesMap().get(fogNode).stream().map(swModule -> optimalDeployment.getApplication().getName() + "/" + swModule.getId()).collect(Collectors.toList());

            deployedFlows.forEach(deployedFlow -> {
                // delete existing flows on node which are not part of the new optimal deployment
                if (!modulesToDeployOnNode.contains(deployedFlow)) {
                    fogNode.getNodeRedController().deleteFlowByName(deployedFlow);
                }
            });
        }

        optimalDeployment.getModuleToNodeMap().entrySet().stream().forEach(entry -> {
            AppSoftwareModule module = entry.getKey();
            NodeRedFogNode node = (NodeRedFogNode) entry.getValue();
            String flowName = String.format("%s/%s", optimalDeployment.getApplication().getName(), module.getId());

            List<String> destinationAddresses = optimalDeployment.getDestinationNodesForSourceModule(module.getId()).stream().map(destinationId -> {
                NodeRedFogNode nrfn = (NodeRedFogNode) infrastructure.getFogNode(destinationId);
                return nrfn.getAddress();
            }).collect(Collectors.toList());

//            System.out.println(String.format("[NodeRedOrchestrator] Going to deploy '%s' on node '%s'; output goes to destination addresses: %s", module.getId(), node.getId(), destinationAddresses));
            try {
                NodeRedFlow flowToDeploy = flowDatabase.getFlowByName(flowName).setDestinations(destinationAddresses);
                flowToDeploy = flowToDeploy.replaceMqttBrokerNodes(null);
                node.getNodeRedController().deployFlow(flowToDeploy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
