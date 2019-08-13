package de.tuberlin.aot.thesis.slienau.orchestrator.monitor;

import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedOrchestrator;

import java.io.IOException;
import java.util.Queue;

public class HeartbeatProcessor implements Runnable {
    private final NodeRedOrchestrator orchestrator;
    private final Queue<Heartbeat> heartbeatQueue;

    public HeartbeatProcessor(NodeRedOrchestrator orchestrator) {
        this.heartbeatQueue = orchestrator.getHeartbeatQueue();
        this.orchestrator = orchestrator;
    }

    @Override
    public void run() {
        System.out.println("[HeartbeatProcessor] Started...");
        while (true) {
            Heartbeat hb;
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
            try {
                orchestrator.handleHeartbeat(hb);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
