package de.tuberlin.aot.thesis.slienau.orchestrator.monitor;

import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedFogNode;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedNetworkUplink;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedOrchestrator;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.NetworkUplink;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class FogNodeMonitor implements Runnable {
    private static final int CHECKING_INTERVAL = 1; // FogNodeMonitor will run every x seconds
    private static final int HEARTBEAT_TIMEOUT = 3; // FogNodeMonitor will check if node is still available if no new heartbeat was received within x seconds
    private static final int HARD_TIMEOUT = 10; // FogNodeMonitor will remove fogNode from infrastructure without checking if no heartbeat was received within x seconds
    private static final int UPLINK_MAX_AGE = 60; // FogNodeMonitor will remeasure uplinks which are older than x seconds
    private final NodeRedFogNode fogNode;

    public FogNodeMonitor(NodeRedFogNode fogNode) {
        this.fogNode = fogNode;
    }

    @Override
    public void run() {
        NodeRedOrchestrator orchestrator = NodeRedOrchestrator.getInstance();
        System.out.println(String.format("[FogNodeMonitor][%s] Thread started", fogNode.getId()));
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
                    System.out.println(String.format("[FogNodeMonitor][%s] Hard timeout of %s seconds exceeded (last heartbeat received %s seconds ago). Going to remove node from infrastructure.", fogNode.getId(), HARD_TIMEOUT, heartbeatAge));
                    orchestrator.removeFogNode(fogNode.getId());
                    break;
                }

                if (HEARTBEAT_TIMEOUT < heartbeatAge) {
                    System.out.println(String.format("[FogNodeMonitor][%s] Timeout of %s seconds exceeded (last heartbeat received %s seconds ago). Going to check if node is reachable.", fogNode.getId(), HEARTBEAT_TIMEOUT, heartbeatAge));
                    try {
                        if (InetAddress.getByName(fogNode.getNodeRedController().getIp()).isReachable(3000)) {
                            System.out.println(String.format("[FogNodeMonitor][%s] Is reachable.", fogNode.getId()));
                            continue;
                        } else {
                            System.out.println(String.format("[FogNodeMonitor][%s] Is not reachable. Going to remove it from infrastructure.", fogNode.getId()));
                            orchestrator.removeFogNode(fogNode.getId());
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // REMEASURE OLD UPLINKS
                for (NetworkUplink nu : fogNode.getUplinks()) {
                    if (nu.getSource() == nu.getDestination())
                        continue; // don't remeasure uplinks to localhost
                    NodeRedNetworkUplink uplink = (NodeRedNetworkUplink) nu;
                    long uplinkAge = ChronoUnit.SECONDS.between(uplink.getMeasurementTime(), LocalDateTime.now());
                    if (uplinkAge > UPLINK_MAX_AGE && uplink.getState() != NodeRedNetworkUplink.NetworkUplinkState.MEASURING) {
//                        System.out.println(String.format("[FogNodeMonitor][%s] Going to remeasure uplink %s", fogNode.getId(), uplink));
                        uplink.measure(true, true);
                        break;
                    }
                }

                Thread.sleep(CHECKING_INTERVAL * 1000);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(String.format("[FogNodeMonitor][%s] Going to terminate thread", fogNode.getId()));
    }
}
