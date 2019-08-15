package de.tuberlin.aot.thesis.slienau.orchestrator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import de.tuberlin.aot.thesis.slienau.models.Heartbeat;
import de.tuberlin.aot.thesis.slienau.models.SystemInfo;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.NetworkUplink;
import de.tuberlin.aot.thesis.slienau.utils.NumberUtils;
import de.tuberlin.aot.thesis.slienau.utils.SchedulerUtils;

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
        try {
            // delete all flows on new node (in case they have "old" flows deployed which could disturb the current deployment strategy)
            nodeRedController.deleteAllFlows();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(String.format("tcp://%s:52376", address)).build();
        dockerClient = DockerClientBuilder.getInstance(config).build();
        this.setSystemInfoFromNodeRed();

        // remove "unlimited" uplink and measure bandwidth to self
        super.removeUplinkTo(this.getId());
        double mbitsToSelf = this.measureBandwidthTo(this.getAddress());
        super.addUplink(new NetworkUplink(this, this, 0, SchedulerUtils.mbitToBit(mbitsToSelf)));

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
            super.addConnectedHardware(systemInfo.getConnectedHardware());
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

//    /**
//     * Runs iperf3 from this node to destination node. Returns bandwidth in Mbit/s
//     *
//     * @param destinationAddress
//     * @return
//     */
//    public double getBandwidthTo(String destinationAddress) {
//        String payload = destinationAddress + " | grep 'receiver' | awk '{print $7}'";
//        byte[] bandwidthResult = this.executeMqttCommand("iperf3", payload.getBytes());
//        try {
//            return NumberUtils.stringToDouble(new String(bandwidthResult));
//        } catch (ParseException e) {
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * Measures bandwidth to destination node via HTTP
     *
     * @param destinationAddress ip or hostname of destination node
     * @return bandwidth from this node to destination node in Mbit/s
     */
    public double measureBandwidthTo(String destinationAddress) {
        byte[] bandwidthResultByte = this.executeMqttCommand("bandwidth", destinationAddress.getBytes());
        try {
            JsonNode bandwidthResult = OBJECT_MAPPER.readTree(bandwidthResultByte);
            return bandwidthResult.path("mbitPerSecond").doubleValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Double.MAX_VALUE;
    }

    private void setCpuScoreFromBenchmark() {
        byte[] benchmarkResultBytes = this.executeMqttCommand("benchmark_cpu");
        String benchmarkResultString = new String(benchmarkResultBytes)
                .replace("s", "")
                .replace("\n", "")
                .replace("\r", "");
        try {
            double time = NumberUtils.stringToDouble(benchmarkResultString);
            int cpuScore = (int) (10000 / time);
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
            executorThread.join(60 * 1000); // timeout of 60s
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
