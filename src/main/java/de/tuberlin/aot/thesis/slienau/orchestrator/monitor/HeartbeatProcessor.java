package de.tuberlin.aot.thesis.slienau.orchestrator.monitor;

import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedFogNode;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedNetworkUplink;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedOrchestrator;
import de.tuberlin.aot.thesis.slienau.orchestrator.models.Heartbeat;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.Infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class HeartbeatProcessor implements Runnable {

    @Override
    public void run() {
        System.out.println("[HeartbeatProcessor] Started...");
        NodeRedOrchestrator orchestrator = NodeRedOrchestrator.getInstance();
        final Queue<Heartbeat> heartbeatQueue = orchestrator.getHeartbeatQueue();
        final Queue<Heartbeat> initialHeartbeatsQueue = new ConcurrentLinkedQueue<>();
        new InitialHeartbeatHandler(initialHeartbeatsQueue).start();
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
                // update timestamp of heartbeat to current time so that InfrastructureMaintainer can check missing timestamps
                hb.setTimestamp(LocalDateTime.now());
                fogNode.setLatestHeartbeat(hb);
            }

        }
    }

    class InitialHeartbeatHandler extends Thread {

        private final Queue<Heartbeat> initialHeartbeatsQueue;

        public InitialHeartbeatHandler(Queue<Heartbeat> initialHeartbeatsQueue) {
            this.initialHeartbeatsQueue = initialHeartbeatsQueue;
        }

        @Override
        public void run() {
            System.out.println(String.format("[InitialHeartbeatHandler] Started"));
            NodeRedOrchestrator orchestrator = NodeRedOrchestrator.getInstance();
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
                System.out.println(String.format("[InitialHeartbeatHandler] Handling initial heartbeat from %s", initialHeartbeat.getDeviceName()));
                try {
                    NodeRedFogNode newNode = new NodeRedFogNode(
                            initialHeartbeat.getDeviceName(),
                            initialHeartbeat.getPublicFqdn(),
                            initialHeartbeat.getPublicPort(),
                            null
                    );

                    initialHeartbeat.setTimestamp(LocalDateTime.now());
                    newNode.setLatestHeartbeat(initialHeartbeat);

                    // add newNode to infrastructure
                    Infrastructure infrastructure = orchestrator.getInfrastructure();
                    infrastructure.addFogNode(newNode);

                    // Get a all nodes from infrastructure except newNode
                    List<NodeRedFogNode> destinationNodes = infrastructure.getFogNodes().stream()
                            .filter(node -> !node.getId().equals(newNode.getId()))
                            .map(node -> (NodeRedFogNode) node)
                            .collect(Collectors.toList());

                    // add network uplinks from newNode to all other nodes and from other nodes to newNode
                    for (NodeRedFogNode destinationNode : destinationNodes) {
                        // from newNode to other nodes
                        newNode.addUplink(new NodeRedNetworkUplink(
                                newNode,
                                destinationNode,
                                newNode.measureLatencyTo(destinationNode.getIp()),
                                newNode.measureBandwidthTo(destinationNode.getAddress())
                        ));
                        // from other nodes to newNode
                        destinationNode.addUplink(new NodeRedNetworkUplink(
                                destinationNode,
                                newNode,
                                destinationNode.measureLatencyTo(newNode.getIp()),
                                destinationNode.measureBandwidthTo(newNode.getAddress())
                        ));
                    }

                    // check if there is a new optimal deployment after new node and all uplinks have been added
                    orchestrator.checkForNewOptimalDeployment();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
