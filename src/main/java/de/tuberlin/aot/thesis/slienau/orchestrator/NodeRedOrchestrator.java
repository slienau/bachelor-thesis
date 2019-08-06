package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.scheduler.application.Application;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.Infrastructure;
import de.tuberlin.aot.thesis.slienau.scheduler.interfaces.Scheduler;
import de.tuberlin.aot.thesis.slienau.scheduler.strategy.AppDeployment;
import de.tuberlin.aot.thesis.slienau.scheduler.strategy.SchedulerStrategy;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NodeRedOrchestrator {

    private Map<String, NodeRedController> nodeRedInstances = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("[NodeRedOrchestrator] Starting...");
        Infrastructure i = new Infrastructure();
        i.addFogNode("mbp", 1024 * 16, 256, 8, 16000, null);

        Application a = new Application("sensorNetworkApp");

        a.addSoftwareModule("data-reader", "RAW_SENSOR_DATA", "SENSOR_DATA", 50, 0.5, 1000, null);
        a.addSoftwareModule("data-processor", "SENSOR_DATA", "SENSOR_DATA_PROCESSED", 100, 0.5, 5000, null);
        a.addSoftwareModule("data-viewer", "SENSOR_DATA_PROCESSED", null, 100, 0.5, 500, null);

        a.addMessage("RAW_SENSOR_DATA", 10);
        a.addMessage("SENSOR_DATA", 10);
        a.addMessage("SENSOR_DATA_PROCESSED", 10);

        a.addLoop("sensorNetworkLoop1", 500, Arrays.asList("data-reader", "data-processor", "data-viewer"));

        Scheduler s = new SchedulerStrategy(a, i);
        AppDeployment d = s.getFastestDeployment();
        System.out.println(d.createDetailsString());

        NodeRedController mbp = new NodeRedController("mbp", "localhost");
        System.out.println(String.format("Flow 'delete-me' exists: %s", mbp.checkIfFlowExists("delete-me")));
        mbp.deployFlow("delete-me");
        System.out.println(String.format("Flow 'delete-me' exists: %s", mbp.checkIfFlowExists("delete-me")));
        mbp.deployFlow("delete-me");
        System.out.println(String.format("Flow 'delete-me' exists: %s", mbp.checkIfFlowExists("delete-me")));
//        mbp.deleteFlowByName("delete-me");
//        System.out.println(String.format("Flow 'delete-me' exists: %s", mbp.checkIfFlowExists("delete-me")));

    }
}
