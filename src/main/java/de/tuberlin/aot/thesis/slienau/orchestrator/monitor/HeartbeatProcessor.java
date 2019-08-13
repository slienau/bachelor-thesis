package de.tuberlin.aot.thesis.slienau.orchestrator.monitor;

import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedFogNode;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedOrchestrator;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.Infrastructure;
import de.tuberlin.aot.thesis.slienau.utils.NumberUtils;

import java.io.IOException;
import java.util.Queue;

public class HeartbeatProcessor implements Runnable {
    private final NodeRedOrchestrator orchestrator;
    private final Infrastructure infrastructure;
    private final Queue<Heartbeat> heartbeatQueue;

    public HeartbeatProcessor(NodeRedOrchestrator orchestrator, Queue<Heartbeat> heartbeatQueue) {
        this.infrastructure = orchestrator.getInfrastructure();
        this.heartbeatQueue = heartbeatQueue;
        this.orchestrator = orchestrator;
    }

    @Override
    public void run() {
        System.out.println("[HeartbeatProcessor] Started...");
        while (true) {
            Heartbeat hb = null;
            synchronized (heartbeatQueue) {
                while (heartbeatQueue.isEmpty()) {
                    try {
                        heartbeatQueue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                hb = heartbeatQueue.remove();
                heartbeatQueue.notifyAll();
            }
            //                System.out.println(String.format("[HeartbeatProcessor] Received %s", hb));
            if (infrastructure.getFogNode(hb.getDeviceName()) == null) {
                // new node --> add
                NodeRedFogNode newNode = new NodeRedFogNode(
                        hb.getDeviceName(),
                        hb.getDeviceName(),
                        hb.getTotalMem(),
                        NumberUtils.getRandom(16, 100),
                        hb.getCpuCount(),
                        NumberUtils.getRandom(3000, 10000),
                        null
                );

                try {
                    orchestrator.addFogNode(newNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
