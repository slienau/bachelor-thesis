package de.tuberlin.aot.thesis.slienau.orchestrator.monitor;

import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedFogNode;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedNetworkUplink;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedOrchestrator;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.NetworkUplink;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class FogNodeMaintainer implements Runnable {
    private static final int CHECKING_INTERVAL = 1; // FogNodeMaintainer will run every x seconds
    private static final int HEARTBEAT_TIMEOUT = 3; // FogNodeMaintainer will check if node is still available if no new heartbeat was received within x seconds
    private static final int HARD_TIMEOUT = 10; // FogNodeMaintainer will remove fogNode from infrastructure without checking if no heartbeat was received within x seconds
    private final NodeRedFogNode fogNode;

    public FogNodeMaintainer(NodeRedFogNode fogNode) {
        this.fogNode = fogNode;
    }

    @Override
    public void run() {
        NodeRedOrchestrator orchestrator = NodeRedOrchestrator.getInstance();
        System.out.println(String.format("[FogNodeMaintainer][%s] Thread started", fogNode.getId()));
        while (true) {
            try {
                // CHECK IF NODE IS STILL AVAILABLE
                if (fogNode.getLatestHeartbeat() == null) {
                    // initially the heartbeat is null, will be set later
                    Thread.sleep(10);
                    continue;
                }

                // age of latest heartbeat in seconds
                long heartbeatAge = ChronoUnit.SECONDS.between(fogNode.getLatestHeartbeat().getTimestamp(), LocalDateTime.now());

                if (HARD_TIMEOUT < heartbeatAge) {
                    System.out.println(String.format("[FogNodeMaintainer][%s] Hard timeout of %s seconds exceeded (last heartbeat received %s seconds ago). Going to remove node from infrastructure.", fogNode.getId(), HARD_TIMEOUT, heartbeatAge));
                    orchestrator.removeFogNode(fogNode.getId());
                    break;
                }

                if (HEARTBEAT_TIMEOUT < heartbeatAge) {
                    System.out.println(String.format("[FogNodeMaintainer][%s] Timeout of %s seconds exceeded (last heartbeat received %s seconds ago). Going to check if node is reachable.", fogNode.getId(), HEARTBEAT_TIMEOUT, heartbeatAge));
                    try {
                        if (InetAddress.getByName(fogNode.getNodeRedController().getIp()).isReachable(3000)) {
                            System.out.println(String.format("[FogNodeMaintainer][%s] Is reachable.", fogNode.getId()));
                        } else {
                            System.out.println(String.format("[FogNodeMaintainer][%s] Is not reachable. Going to remove it from infrastructure.", fogNode.getId()));
                            orchestrator.removeFogNode(fogNode.getId());
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Thread.sleep(CHECKING_INTERVAL * 1000);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(String.format("[FogNodeMaintainer][%s] Going to terminate thread", fogNode.getId()));
    }
}
