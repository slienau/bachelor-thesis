package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.Heartbeat;
import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.HeartbeatMonitor;
import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.HeartbeatProcessor;
import de.tuberlin.aot.thesis.slienau.scheduler.application.AppSoftwareModule;
import de.tuberlin.aot.thesis.slienau.scheduler.application.Application;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.Infrastructure;
import de.tuberlin.aot.thesis.slienau.scheduler.interfaces.Scheduler;
import de.tuberlin.aot.thesis.slienau.scheduler.strategy.AppDeployment;
import de.tuberlin.aot.thesis.slienau.scheduler.strategy.SchedulerStrategy;
import de.tuberlin.aot.thesis.slienau.utils.NumberUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class NodeRedOrchestrator {

    public static final String MQTT_BROKER = "tcp://localhost:1883";
    private static final NodeRedFlowDatabase flowDatabase = NodeRedFlowDatabase.getInstance();
    private final Infrastructure infrastructure;
    private final Queue<Heartbeat> heartbeatQueue;
    private final Scheduler scheduler;

    public NodeRedOrchestrator() {
        infrastructure = new Infrastructure();
        heartbeatQueue = new ConcurrentLinkedQueue<>();
        scheduler = new SchedulerStrategy(createSensorNetworkApplication(), infrastructure);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[NodeRedOrchestrator] Starting...");

        NodeRedOrchestrator orchestrator = new NodeRedOrchestrator();

        Thread heartbeatMonitorThread = new Thread(new HeartbeatMonitor(MQTT_BROKER, orchestrator.heartbeatQueue));
        heartbeatMonitorThread.start();

        Thread heartbeatProcessorThread = new Thread(new HeartbeatProcessor(orchestrator));
        heartbeatProcessorThread.start();

        Thread infrastructureMaintainerThread = new Thread(new InfrastructureMaintainer(orchestrator));
        infrastructureMaintainerThread.start();
    }

    private static Application createSensorNetworkApplication() {
        Application a = new Application("sensornetwork");

//        a.addHardwareModule("CAMERA", "RAW_SENSOR_DATA");
//        a.addSoftwareModule("data-reader", "RAW_SENSOR_DATA", "SENSOR_DATA", 50, 0.5, 500, Arrays.asList("CAMERA"));
        a.addSoftwareModule("data-reader", "RAW_SENSOR_DATA", "SENSOR_DATA", 50, 0.5, 500, null);
        a.addSoftwareModule("data-processor", "SENSOR_DATA", "SENSOR_DATA_PROCESSED", 100, 0.5, 5000, null);
        a.addSoftwareModule("data-viewer", "SENSOR_DATA_PROCESSED", null, 100, 0.5, 500, null);

        a.addMessage("RAW_SENSOR_DATA", 10);
        a.addMessage("SENSOR_DATA", 10);
        a.addMessage("SENSOR_DATA_PROCESSED", 10);

        a.addLoop("sensorNetworkLoop1", 999999, Arrays.asList("data-reader", "data-processor", "data-viewer"));
        return a;
    }

    public Infrastructure getInfrastructure() {
        return infrastructure;
    }

    public void handleHeartbeat(Heartbeat hb) throws IOException {
        NodeRedFogNode fogNode = (NodeRedFogNode) infrastructure.getFogNode(hb.getDeviceName());
        if (fogNode == null) {
            // new node --> add
            NodeRedFogNode newNode = new NodeRedFogNode(
                    hb.getDeviceName(),
                    hb.getDeviceName(),
                    null
            );

            hb.setTimestamp(LocalDateTime.now());
            newNode.setLatestHeartbeat(hb);
            this.addFogNode(newNode);
        } else {
            hb.setTimestamp(LocalDateTime.now());
            fogNode.setLatestHeartbeat(hb);
        }
    }

    public void addFogNode(NodeRedFogNode newNode) throws IOException {
        // delete all flows on new node (in case they have "old" flows deployed which could disturb the current deployment strategy)
        newNode.getNodeRedController().deleteAllFlows();

        // add to infrastructure
        infrastructure.addFogNode(newNode);

        // add network uplinks to all other nodes
        List<String> destinationNodeIds = infrastructure.getFogNodes().stream()
                .filter(node -> !node.getId().equals(newNode.getId()))
                .map(FogNode::getId)
                .collect(Collectors.toList());
        for (String destinationNodeId : destinationNodeIds) {
            infrastructure.addNetworkLink(
                    newNode.getId(),
                    destinationNodeId,
                    NumberUtils.getRandom(2, 200),
                    NumberUtils.getRandom(10, 250),
                    NumberUtils.getRandom(10, 250)
            );
        }

        // deploy
        this.deployFastestDeployment();
    }

    public void removeFogNode(String fogNodeIdToRemove) throws IOException {
        // remove all uplinks to this node
        for (FogNode fn : infrastructure.getFogNodes()) {
            fn.removeUplinkTo(fogNodeIdToRemove);
        }

        // remove node from infrastructure
        infrastructure.removeFogNode(fogNodeIdToRemove);

        this.deployFastestDeployment();
    }

    public Queue<Heartbeat> getHeartbeatQueue() {
        return heartbeatQueue;
    }

    private void deployFastestDeployment() throws IOException {
        AppDeployment d = this.scheduler.getFastestDeployment();
        if (d == null)
            System.out.println("No deployment found for application!");
        if (d != null) {
            System.out.println(d.createDetailsString());

            for (FogNode fn : infrastructure.getFogNodes()) {
                NodeRedFogNode fogNode = (NodeRedFogNode) fn;
                fogNode.getNodeRedController().deleteAllFlows();
            }

            d.getModuleToNodeMap().entrySet().stream().forEach(entry -> {
                AppSoftwareModule module = entry.getKey();
                NodeRedFogNode node = (NodeRedFogNode) entry.getValue();
                String flowName = String.format("%s/%s", d.getApplication().getName(), module.getId());

                List<String> destinationAddresses = d.getDestinationNodesForSourceModule(module.getId()).stream().map(destinationId -> {
                    NodeRedFogNode nrfn = (NodeRedFogNode) infrastructure.getFogNode(destinationId);
                    return nrfn.getAddress();
                }).collect(Collectors.toList());

                System.out.println(String.format("[NodeRedOrchestrator] Going to deploy '%s' on node '%s'; output goes to destination addresses: %s", module.getId(), node.getId(), destinationAddresses));
                try {
                    NodeRedFlow flowToDeploy = flowDatabase.getFlowByName(flowName).setDestinations(destinationAddresses);
                    node.getNodeRedController().deployFlow(flowToDeploy);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
