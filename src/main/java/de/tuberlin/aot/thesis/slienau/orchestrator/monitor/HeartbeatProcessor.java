package de.tuberlin.aot.thesis.slienau.orchestrator.monitor;

import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedFogNode;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedOrchestrator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HeartbeatProcessor implements Runnable {
    private final NodeRedOrchestrator orchestrator;
    private final Queue<Heartbeat> heartbeatQueue;
    private final Queue<Heartbeat> initialHeartbeatsQueue;

    public HeartbeatProcessor(NodeRedOrchestrator orchestrator) {
        this.heartbeatQueue = orchestrator.getHeartbeatQueue();
        this.orchestrator = orchestrator;
        this.initialHeartbeatsQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        System.out.println("[HeartbeatProcessor] Started...");
        new InitialHeartbeatHandler(orchestrator, initialHeartbeatsQueue).start();
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

            NodeRedFogNode fogNode = (NodeRedFogNode) orchestrator.getInfrastructure().getFogNode(hb.getDeviceName());
            if (fogNode == null) {
                // new node --> add to processing queue
                synchronized (initialHeartbeatsQueue) {
                    initialHeartbeatsQueue.offer(hb);
                    initialHeartbeatsQueue.notifyAll();
                }
            } else {
                hb.setTimestamp(LocalDateTime.now());
                fogNode.setLatestHeartbeat(hb);
            }

        }
    }

    class InitialHeartbeatHandler extends Thread {

        private final NodeRedOrchestrator orchestrator;
        private final Queue<Heartbeat> initialHeartbeatsQueue;

        public InitialHeartbeatHandler(NodeRedOrchestrator orchestrator, Queue<Heartbeat> initialHeartbeatsQueue) {
            this.orchestrator = orchestrator;
            this.initialHeartbeatsQueue = initialHeartbeatsQueue;
        }

        @Override
        public void run() {
            System.out.println(String.format("[InitialHeartbeatHandler] Started"));
            while (true) {
                Heartbeat initialHeartbeat;
                synchronized (initialHeartbeatsQueue) {
                    while (initialHeartbeatsQueue.isEmpty()) {
                        try {
                            initialHeartbeatsQueue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    initialHeartbeat = initialHeartbeatsQueue.remove();
                    initialHeartbeatsQueue.notifyAll();
                }

                if (orchestrator.getInfrastructure().checkIfFogNodeExists(initialHeartbeat.getDeviceName())) {
                    // continue if node was added in the meantime
                    continue;
                }
                System.out.println(String.format("[InitialHeartbeatHandler] Handling %s", initialHeartbeat));
                try {
                    NodeRedFogNode newNode = new NodeRedFogNode(
                            initialHeartbeat.getDeviceName(),
                            initialHeartbeat.getDeviceName(),
                            null
                    );

                    initialHeartbeat.setTimestamp(LocalDateTime.now());
                    newNode.setLatestHeartbeat(initialHeartbeat);
                    orchestrator.addFogNode(newNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
