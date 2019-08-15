package de.tuberlin.aot.thesis.slienau.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import de.tuberlin.aot.thesis.slienau.models.Heartbeat;
import de.tuberlin.aot.thesis.slienau.models.SystemInfo;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;
import de.tuberlin.aot.thesis.slienau.utils.NumberUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.List;

public class NodeRedFogNode extends FogNode {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final NodeRedController nodeRedController;
    private final DockerClient dockerClient;
    private Heartbeat latestHeartbeat;

    public NodeRedFogNode(String id, String address, List<String> connectedHardware) {
        super(id, connectedHardware);
        nodeRedController = new NodeRedController(id, address);
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(String.format("tcp://%s:52376", address)).build();
        dockerClient = DockerClientBuilder.getInstance(config).build();
        this.setSystemInfoFromNodeRed();
        this.setCpuScoreFromBenchmark();
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

    private void setSystemInfoFromNodeRed() {
        try {
            SystemInfo systemInfo = OBJECT_MAPPER.readValue(executeMqttCommand("sysinfo"), SystemInfo.class);
            super.setCpuCores(systemInfo.getCpuCount());
            super.setRamTotal(systemInfo.getFreeMem());
            super.setStorageTotal(16.0f); // TODO: set storage from SystemInfo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLatencyToDestination(String destinationAddress) {
        String payload = destinationAddress + " | tail -1| awk '{print $4}' | cut -d '/' -f 2";
        byte[] resultBytes = this.executeMqttCommand("ping", payload.getBytes());
        try {
            return (int) NumberUtils.stringToDouble(new String(resultBytes)) + 1; // +1 to "round up"
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs iperf3 from this node to destination node. Returns bandwidth in Mbit/s
     *
     * @param destinationAddress
     * @return
     */
    public double getBandwidthTo(String destinationAddress) {
        String payload = destinationAddress + " | grep 'receiver' | awk '{print $7}'";
        byte[] bandwidthResult = this.executeMqttCommand("iperf3", payload.getBytes());
        try {
            return NumberUtils.stringToDouble(new String(bandwidthResult));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void setCpuScoreFromBenchmark() {
        byte[] benchmarkResultBytes = this.executeMqttCommand("benchmark_cpu");
        String benchmarkResultString = new String(benchmarkResultBytes)
                .replace("s", "")
                .replace("\n", "")
                .replace("\r", "");
        try {
            double time = NumberUtils.stringToDouble(benchmarkResultString);
            int cpuScore = (int) (50000 / time);
            System.out.println(String.format("[NodeRedFogNode][%s] Benchmark result CPU score: %s", this.getId(), cpuScore));
            super.setCpuInstructionsPerSecond(cpuScore);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isReachable() {
        try {
            return InetAddress.getByName(nodeRedController.getNodeRedAddress()).isReachable(3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private byte[] executeMqttCommand(String command) {
        return executeMqttCommand(command, null);
    }

    private byte[] executeMqttCommand(String command, byte[] payload) {
        MqttCommandExecutor executor = new MqttCommandExecutor(NodeRedOrchestrator.MQTT_BROKER, this.getId(), command, payload);
        Thread executorThread = new Thread(executor);
        executorThread.start();
        try {
            executorThread.join();
            return executor.getResult();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "NodeRedFogNode{" +
                "fogNode=" + super.toString() +
                ", nodeRedController=" + nodeRedController +
                '}';
    }
}
