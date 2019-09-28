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

/**
 * Handles incoming heartbeats sent by the fog nodes
 */
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
            // read 'heartbeatQueue'. HeartbeatMonitor fills this queue with the arriving heartbeats
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

            // get FogNode instance of heartbeat
            NodeRedFogNode fogNode = (NodeRedFogNode) orchestrator.getInfrastructure().getFogNode(hb.getDeviceName());
            if (fogNode == null) {
                // no fog node in infrastructure --> new node --> add to 'initialHeartbeatsQueue' which is handles by the 'InitialHeartbeatHandler'
                synchronized (initialHeartbeatsQueue) {
                    initialHeartbeatsQueue.offer(hb);
                    initialHeartbeatsQueue.notifyAll();
                }
            } else {
                // fog node already in infrastructure
                // --> update timestamp of heartbeat to current time so that InfrastructureMaintainer can check missing timestamps
                hb.setTimestamp(LocalDateTime.now());
                fogNode.setLatestHeartbeat(hb);
            }

        }
    }

    /**
     * Handles new heartbeats from nodes which are not in the infrastructure yet
     */
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
                // get initial heartbeat from queue
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

                // handle new heartbeat
                System.out.println(String.format("[InitialHeartbeatHandler] Handling initial heartbeat from %s", initialHeartbeat.getDeviceName()));
                try {
                    // create new node. during the instantiation, system information like CPU, RAM, connected hardware, ... is retrieved
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

                    // Get a all nodes from infrastructure except newNode (for measuring the uplinks in the next step)
                    List<NodeRedFogNode> destinationNodes = infrastructure.getFogNodes().stream()
                            .filter(node -> !node.getId().equals(newNode.getId()))
                            .map(node -> (NodeRedFogNode) node)
                            .collect(Collectors.toList());

                    // add network uplinks from newNode to all other nodes and from all other nodes to newNode
                    for (NodeRedFogNode destinationNode : destinationNodes) {

                        // from newNode to other node
                        NodeRedNetworkUplink newUplink1 = new NodeRedNetworkUplink(newNode, destinationNode);
                        newUplink1.measure(true, true); // measure bandwidth and latency of new uplink
                        newNode.addUplink(newUplink1);

                        // from other node to newNode
                        NodeRedNetworkUplink newUplink2 = new NodeRedNetworkUplink(destinationNode, newNode);
                        newUplink2.measure(true, true);
                        destinationNode.addUplink(newUplink2);
                    }

                    // check if there is a new optimal deployment after new node and all uplinks have been initialized
                    orchestrator.checkForNewOptimalDeployment();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
