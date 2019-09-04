package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class InfrastructureMaintainer implements Runnable {
    private static final int CHECKING_INTERVAL = 1; // in seconds
    private static final int HEARTBEAT_TIMEOUT = 3; // in seconds
    private static final int HARD_TIMEOUT = 10;

    private final NodeRedOrchestrator orchestrator;

    public InfrastructureMaintainer(NodeRedOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void run() {
        while (true) {
            try {
                for (FogNode fn : orchestrator.getInfrastructure().getFogNodes()) {
                    NodeRedFogNode fogNode = (NodeRedFogNode) fn;
                    long secondsBetween = ChronoUnit.SECONDS.between(fogNode.getLatestHeartbeat().getTimestamp(), LocalDateTime.now());

                    if (HARD_TIMEOUT < secondsBetween) {
                        System.out.println(String.format("[InfrastructureMaintainer][%s] Hard timeout of %s seconds exceeded (last heartbeat received %s seconds ago). Going to remove node from infrastructure.", fogNode.getId(), HARD_TIMEOUT, secondsBetween));
                        orchestrator.removeFogNode(fogNode.getId());
                    }

                    if (HEARTBEAT_TIMEOUT < secondsBetween) {
                        System.out.println(String.format("[InfrastructureMaintainer][%s] Timeout of %s seconds exceeded (last heartbeat received %s seconds ago). Going to check if node is reachable.", fogNode.getId(), HEARTBEAT_TIMEOUT, secondsBetween));
                        boolean isReachable = fogNode.isReachable();
                        if (isReachable) {
                            System.out.println(String.format("[InfrastructureMaintainer][%s] Is reachable.", fogNode.getId()));
                            continue;
                        }
                        System.out.println(String.format("[InfrastructureMaintainer][%s] Is not reachable. Going to remove it from infrastructure.", fogNode.getId()));
                        orchestrator.removeFogNode(fogNode.getId());
                    }
                }
                Thread.sleep(CHECKING_INTERVAL * 1000);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
