package de.tuberlin.aot.thesis.slienau.orchestrator;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;

import java.util.List;

public class NodeRedFogNode extends FogNode {

    private final NodeRedController nodeRedController;
    private final DockerClient dockerClient;

    public NodeRedFogNode(String id, String address, int ramTotal, int storageTotal, int cpuCores, int cpuInstructionsPerSecond, List<String> connectedHardware) {
        super(id, ramTotal, storageTotal, cpuCores, cpuInstructionsPerSecond, connectedHardware);
        nodeRedController = new NodeRedController(id, address);
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(String.format("tcp://%s:52376", address)).build();
        dockerClient = DockerClientBuilder.getInstance(config).build();
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

    @Override
    public String toString() {
        return "NodeRedFogNode{" +
                "nodeRedController=" + nodeRedController +
                ", fogNode=" + super.toString() +
                '}';
    }
}
