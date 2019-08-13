package de.tuberlin.aot.thesis.slienau.orchestrator.monitor;

import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedFogNode;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.Infrastructure;
import de.tuberlin.aot.thesis.slienau.utils.NumberUtils;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class HeartbeatProcessor implements Runnable {
    private final Infrastructure infrastructure;
    private final Queue<Heartbeat> heartbeatQueue;

    public HeartbeatProcessor(Infrastructure infrastructure, Queue<Heartbeat> heartbeatQueue) {
        this.infrastructure = infrastructure;
        this.heartbeatQueue = heartbeatQueue;
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
                    // delete all flows on new node (in case they have "old" flows deployed which could disturb the current deployment strategy)
                    newNode.getNodeRedController().deleteAllFlows();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                infrastructure.addFogNode(newNode);

                // add network uplinks to all other nodes
                List<String> destinationNodeIds = infrastructure.getFogNodes().stream()
                        .filter(node -> !node.getId().equals(newNode.getId()))
                        .map(node -> node.getId())
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
            }
        }
    }
}
