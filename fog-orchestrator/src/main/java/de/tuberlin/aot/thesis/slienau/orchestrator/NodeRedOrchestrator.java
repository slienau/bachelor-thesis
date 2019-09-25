package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.orchestrator.models.Heartbeat;
import de.tuberlin.aot.thesis.slienau.orchestrator.models.NodeRedFlow;
import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.HeartbeatMonitor;
import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.HeartbeatProcessor;
import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.QoSMonitor;
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
    private final Application application;
    private AppDeployment optimalDeployment;

    private NodeRedOrchestrator() {
        infrastructure = new Infrastructure();
        heartbeatQueue = new ConcurrentLinkedQueue<>();
//        application = createSensorNetworkApplication();
        application = createObjectDetectionApplication();
        scheduler = new QosScheduler(application, infrastructure);
    }

    public static void main(String[] args) {
        System.out.println("[NodeRedOrchestrator] Starting...");
        NodeRedOrchestrator.getInstance();

        new Thread(new HeartbeatMonitor()).start();
        new Thread(new HeartbeatProcessor()).start();
        new Thread(new QoSMonitor()).start();
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

        // adding software module "webapp"
        a.addSoftwareModule("webapp", // module name
                null, // module input
                "IMAGE_UNDETECTED", // module output
                0, // required RAM
                0, // required storage
                0, // required CPU instructions to process one message
                Arrays.asList("CAMERA")
        );


        // adding software module "detector"

        // execution time of detector on vm-02: 1317 ms
        // CPU score of vm-02: 11661
        double requiredMiForDetector = 11661 * (1317d / 1000d);

        a.addSoftwareModule(
                "detector", // module name
                "IMAGE_UNDETECTED", // module input
                "IMAGE_DETECTED", // module output
                0, // required RAM
                0, // required storage
                (int) requiredMiForDetector, // required instructions to process one message
                Arrays.asList("OD-DOCKER-CONTAINER") // required hardware modules
        );

        a.addMessage("IMAGE_UNDETECTED", 1383); // sample image (original), 1.383 KB
        a.addMessage("IMAGE_DETECTED", 142); // sample image (detected), 142 KB

        a.addLoop("objectDetectionLoop1",
                5000, // max latency
                Arrays.asList("webapp", "detector", "webapp"), // module sequence
                true // allow mismatching input/output types
        );

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

    public Application getApplication() {
        return application;
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
            //System.out.println("[NodeRedOrchestrator] No new optimal deployment found!");
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
                String destinationAddress = nrfn.getAddress();
                if (destinationAddress.equals(node.getAddress()))
                    destinationAddress = "127.0.0.1:1880";  // if destination is the same node (as source), then send it to localhost (traffic stays inside docker container)
                return destinationAddress;
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
