package de.tuberlin.aot.thesis.slienau.orchestrator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.aot.thesis.slienau.orchestrator.models.Heartbeat;
import de.tuberlin.aot.thesis.slienau.orchestrator.models.SystemInfo;
import de.tuberlin.aot.thesis.slienau.orchestrator.monitor.FogNodeMonitor;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;
import de.tuberlin.aot.thesis.slienau.utils.SchedulerUtils;

import java.io.IOException;
import java.util.List;

public class NodeRedFogNode extends FogNode {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final NodeRedController nodeRedController;
    private Heartbeat latestHeartbeat;

    public NodeRedFogNode(String id, String ip, int port, List<String> connectedHardware) {
        super(id, connectedHardware);
        nodeRedController = new NodeRedController(id, ip, port);
        System.out.println(String.format("[NodeRedFogNode][%s] Created new instance", this.getId()));
        initialize();
    }

    private void initialize() {
        try {
            // delete all flows on new node (in case they have "old" unwanted flows deployed which could disturb the current deployment strategy)
            nodeRedController.deleteAllFlows();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.getAndSetSysinfo();

        // remove "unlimited" uplink to localhost (used in algorithm) and measure bandwidth instead
        super.removeUplinkTo(this.getId());
        double mbitsToSelf = this.measureBandwidthTo(this.getAddress());
        super.addUplink(new NodeRedNetworkUplink(this, this, 0, mbitsToSelf));

        // measure benchmark
        this.getAndSetCpuScore();

        System.out.println(String.format("[NodeRedFogNode][%s] Initialization complete %s", this.getId(), this));

        // Start FogNodeMonitor thread which monitors this node
        new Thread(new FogNodeMonitor(this)).start();
    }

    public NodeRedController getNodeRedController() {
        return nodeRedController;
    }

    public String getAddress() {
        return String.format("%s:%s", nodeRedController.getIp(), nodeRedController.getPort());
    }

    public Heartbeat getLatestHeartbeat() {
        return latestHeartbeat;
    }

    public void setLatestHeartbeat(Heartbeat latestHeartbeat) {
        this.latestHeartbeat = latestHeartbeat;
    }

    private void getAndSetSysinfo() {
        try {
            SystemInfo systemInfo = OBJECT_MAPPER.readValue(executeMqttCommand("sysinfo"), SystemInfo.class);
            super.setCpuCores(systemInfo.getCpuCount());
            super.setRamTotal(systemInfo.getTotalMem());
            super.setStorageTotal(systemInfo.getTotalDisk());
            super.addConnectedHardware(systemInfo.getConnectedHardware());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int measureLatencyTo(String destinationIp) throws IOException {
        if (this.getIp().equals(destinationIp)) {
            return 0; // latency to localhost is 0
        }
        byte[] resultBytes = this.executeMqttCommand("ping", destinationIp.getBytes(), 10);
        if (resultBytes == null)
            throw new IOException(String.format("[NodeRedFogNode][%s] Latency measurement to %s failed", this.getId(), destinationIp));
        return (int) OBJECT_MAPPER.readTree(resultBytes).path("time").doubleValue() + 1; // +1 to "round up"
    }

//    /**
//     * Runs iperf3 from this node to destination node. Returns bandwidth in Mbit/s
//     *
//     * @param destinationAddress
//     * @return
//     */
//    public double getBandwidthTo(String destinationAddress) {
//        String payload = destinationAddress;
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
        final double MIN_TIME = 1000; // If a measurement takes less than this, a new measurement will be executed to get a more accurate result
        final int MAX_MEASUREMENTS = 2; // max. amount of measurements, regardless of MIN_TIME
        int measurement = 1; // measurements counter

        // prepare payload
        ObjectNode cmdPayload = OBJECT_MAPPER.createObjectNode();
        cmdPayload.put("destination", destinationAddress);
        int size = 2 * 1024; // start with 2 MB size
        if (this.getAddress().equals(destinationAddress)) {
            size = 10 * 1024; // start with 10 MB size if measurement if from this node to this node
            destinationAddress = "127.0.0.1:1880"; // send to 127.0.0.1 (stay inside docker container)
        }
        cmdPayload.put("destination", destinationAddress);

        while (true) {
            try {
                cmdPayload.put("size", size);
                byte[] bandwidthResultByte = this.executeMqttCommand("bandwidth", OBJECT_MAPPER.writeValueAsString(cmdPayload).getBytes());
                JsonNode bandwidthResult = OBJECT_MAPPER.readTree(bandwidthResultByte);
                double time = bandwidthResult.path("time").doubleValue();
                double mbits = bandwidthResult.path("mbitPerSecond").doubleValue();
                // System.out.println(String.format("[NodeRedFogNode][%s] Bandwidth to %s is %sMbit/s (test size: %sKB; time: %sms)", this.getId(), destinationAddress, mbits, size, time));
                if (time >= MIN_TIME || measurement++ >= MAX_MEASUREMENTS)
                    return mbits;
                // increase size if execution took less than MIN_TIME to get a more accurate result
                if (time < MIN_TIME * 0.33)
                    size = size * 4;
                else
                    size = size * 2;
            } catch (IOException e) {
                e.printStackTrace();
                return Double.MAX_VALUE;
            }
        }
    }

    private void getAndSetCpuScore() {
        // Execute benchmark command on node
        byte[] benchmarkResultBytes = this.executeMqttCommand("benchmark_cpu");
        try {
            int cpuScore = OBJECT_MAPPER.readTree(benchmarkResultBytes).path("cpuScore").intValue();
            System.out.println(String.format("[NodeRedFogNode][%s] Benchmark result CPU score: %s", this.getId(), cpuScore));
            this.setCpuMips(cpuScore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] executeMqttCommand(String command) {
        return executeMqttCommand(command, null);
    }

    private byte[] executeMqttCommand(String command, byte[] payload) {
        int timeout = 30; // timeout of 30 seconds
        return executeMqttCommand(command, payload, timeout);
    }

    private byte[] executeMqttCommand(String command, byte[] payload, int timeout) {
        MqttCommandExecutor executor = new MqttCommandExecutor(NodeRedOrchestrator.MQTT_BROKER, this.getId(), command, payload);
        Thread executorThread = new Thread(executor);
        executorThread.start();
        try {
            executorThread.join(timeout * 1000);
            return executor.getResult();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new NullPointerException(String.format("[NodeRedFogNode] MQTT Command '%s' not executed within %s seconds", command, timeout));
        }
    }

    @Override
    public String toString() {
        return "NodeRedFogNode{" +
                "ip=" + nodeRedController.getIp() +
                ", port=" + nodeRedController.getPort() +
                ", fogNode=" + super.toString() +
                '}';
    }

    public String getIp() {
        return nodeRedController.getIp();
    }
}
