package de.tuberlin.aot.thesis.slienau.orchestrator;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.Heartbeat;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class NodeRedFogNode extends FogNode {

    private final NodeRedController nodeRedController;
    private final DockerClient dockerClient;
    private Heartbeat latestHeartbeat;

    public NodeRedFogNode(String id, String address, float ramTotal, float storageTotal, int cpuCores, int cpuInstructionsPerSecond, List<String> connectedHardware) {
        super(id, ramTotal, storageTotal, cpuCores, cpuInstructionsPerSecond, connectedHardware);
        nodeRedController = new NodeRedController(id, address);
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(String.format("tcp://%s:52376", address)).build();
        dockerClient = DockerClientBuilder.getInstance(config).build();
        System.out.println(String.format("[NodeRedFogNode] Created new instance %s", this));
    }

    public NodeRedController getNodeRedController() {
        return nodeRedController;
    }

    public String getAddress() {
        return nodeRedController.getNodeRedAddress();
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    public Heartbeat getLatestHeartbeat() {
        return latestHeartbeat;
    }

    public void setLatestHeartbeat(Heartbeat latestHeartbeat) {
        this.latestHeartbeat = latestHeartbeat;
    }

    public boolean isReachable() {
        try {
            return InetAddress.getByName(nodeRedController.getNodeRedAddress()).isReachable(3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeRedFogNode{" +
                "fogNode=" + super.toString() +
                ", nodeRedController=" + nodeRedController +
                '}';
    }
}
