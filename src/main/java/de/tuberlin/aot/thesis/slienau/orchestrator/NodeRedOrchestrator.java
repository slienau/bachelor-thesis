package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.scheduler.application.AppSoftwareModule;
import de.tuberlin.aot.thesis.slienau.scheduler.application.Application;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.Infrastructure;
import de.tuberlin.aot.thesis.slienau.scheduler.interfaces.Scheduler;
import de.tuberlin.aot.thesis.slienau.scheduler.strategy.AppDeployment;
import de.tuberlin.aot.thesis.slienau.scheduler.strategy.SchedulerStrategy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NodeRedOrchestrator {

    private static final NodeRedFlowDatabase flowDatabase = NodeRedFlowDatabase.getInstance();
    private final Infrastructure infrastructure;

    public NodeRedOrchestrator() {
        infrastructure = new Infrastructure();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("[NodeRedOrchestrator] Starting...");

        NodeRedOrchestrator orchestrator = new NodeRedOrchestrator();
        Infrastructure i = orchestrator.infrastructure;
        Application a = createSensorNetworkApplication();

        NodeRedFogNode mbp = new NodeRedFogNode("mbp", "dsl-mbp", 1024 * 16, 256, 8, 16000, null);
        NodeRedFogNode raspi1 = new NodeRedFogNode("raspi-01", "raspi-01", 1024 * 1, 32, 4, 1000, Arrays.asList("CAMERA"));
        NodeRedFogNode raspi2 = new NodeRedFogNode("raspi-02", "raspi-02", 1024 * 4, 16, 4, 3000, null);
        i.addFogNode(mbp);
        i.addFogNode(raspi1);
        i.addFogNode(raspi2);

        i.addNetworkLink("mbp", "raspi-01", 7, 240.0, 240.0);
        i.addNetworkLink("mbp", "raspi-02", 6, 280.0, 280.0);
        i.addNetworkLink("raspi-01", "raspi-02", 1, 250.0, 250.0);

        Scheduler s = new SchedulerStrategy(a, i);
        AppDeployment d = s.getFastestDeployment();
        System.out.println(d.createDetailsString());

        for (FogNode fn : i.getFogNodes()) {
            NodeRedFogNode fogNode = (NodeRedFogNode) fn;
            fogNode.getNodeRedController().deleteAllFlows();
        }


        d.getModuleToNodeMap().entrySet().stream().forEach(entry -> {
            AppSoftwareModule module = entry.getKey();
            NodeRedFogNode node = (NodeRedFogNode) entry.getValue();
            String flowName = String.format("%s/%s", d.getApplication().getName(), module.getId());

            List<String> destinationAddresses = d.getDestinationNodesForSourceModule(module.getId()).stream().map(destinationId -> {
                NodeRedFogNode nrfn = (NodeRedFogNode) i.getFogNode(destinationId);
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

    private static Application createSensorNetworkApplication() {
        Application a = new Application("sensornetwork");

        a.addHardwareModule("CAMERA", "RAW_SENSOR_DATA");
        a.addSoftwareModule("data-reader", "RAW_SENSOR_DATA", "SENSOR_DATA", 50, 0.5, 500, Arrays.asList("CAMERA"));
        a.addSoftwareModule("data-processor", "SENSOR_DATA", "SENSOR_DATA_PROCESSED", 100, 0.5, 5000, null);
        a.addSoftwareModule("data-viewer", "SENSOR_DATA_PROCESSED", null, 100, 0.5, 500, null);

        a.addMessage("RAW_SENSOR_DATA", 10);
        a.addMessage("SENSOR_DATA", 10);
        a.addMessage("SENSOR_DATA_PROCESSED", 10);

        a.addLoop("sensorNetworkLoop1", 1500, Arrays.asList("data-reader", "data-processor", "data-viewer"));
        return a;
    }
}
