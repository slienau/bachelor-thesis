package algorithm.deployment;

import algorithm.application.AppMessage;
import algorithm.application.AppModule;
import algorithm.application.Application;
import algorithm.infrastructure.FogNode;
import algorithm.infrastructure.NetworkUplink;

import java.util.*;
import java.util.stream.Collectors;

public class AppDeployment {
    private final Application application;
    private final Map<AppModule, FogNode> moduleToNodeMap;
    private final boolean valid;
    private final double totalLatency;

    AppDeployment(Application application, Map<AppModule, FogNode> moduleToNodeMap) {
        this.application = application;
        this.moduleToNodeMap = moduleToNodeMap;
        this.valid = this.checkValidity();
        this.totalLatency = this.calculateTotalLatency();
    }

    public boolean isValid() {
        return valid;
    }

    public double getTotalLatency() {
        return totalLatency;
    }

    private boolean checkValidity() {
//        System.out.println(String.format("[AppDeployment] Validating %s", this));
        if (!this.validateHardwareRequirements())
            return false; // no further checking if hardware requirement check already failed
        if (!this.validateLatencyRequirements())
            return false;
//        System.out.println(String.format("[AppDeployment] Valid %s", this));
        return true;
    }

    private boolean validateHardwareRequirements() {
        boolean valid = true;
        // validate hardware requirements
        for (Map.Entry<AppModule, FogNode> entry : moduleToNodeMap.entrySet()) {
            AppModule module = entry.getKey();
            FogNode node = entry.getValue();
            if (!node.deployModule(module)) {
                valid = false;
                break;
            }
        }
        // Undeploy all modules from all nodes to free resources for further hardware validity checks
        this.undeployAllModulesFromNodes();

//        if (valid)
//            System.out.println(String.format("[AppDeployment] [Hardware Requirement Check] SUCCESS for %s", this));
//        else
//            System.out.println(String.format("[AppDeployment] [Hardware Requirement Check] FAILED for %s", this));

        return valid;
    }

    private boolean validateLatencyRequirements() {
        double totalLatency = this.calculateTotalLatency();
        boolean valid = totalLatency < this.application.getMaxLatency();

//        if (valid)
//            System.out.println(String.format("[AppDeployment] [Latency Requirement Check] SUCCESS (Required: %s; Has: %s)", this.application.getMaxLatency(), totalLatency));
//        else
//            System.out.println(String.format("[AppDeployment] [Latency Requirement Check] FAILED (Required: %s; Has: %s)", this.application.getMaxLatency(), totalLatency));
        return valid;
    }

    private double calculateTotalLatency() {
        return calculateTotalTransferTime() + calculateTotalProcessingTime();
    }

    private double calculateTotalProcessingTime() {
        return moduleToNodeMap.entrySet().stream().mapToDouble(entry -> entry.getValue().calculateProcessingTimeForModule(entry.getKey())).sum();
    }

    private double calculateTotalTransferTime() {
//        System.out.println(String.format("[Calculating total transfer time] for %s", this));
        double totalTransferTime = 0;
        int messageCount = 0;
        for (AppMessage message : application.getMessages()) {
            AppModule sourceModule = message.getSource();
            AppModule destinationModule = message.getDestination();
            FogNode sourceNode = moduleToNodeMap.get(sourceModule);
            FogNode destinationNode = moduleToNodeMap.get(destinationModule);

            NetworkUplink uplink = sourceNode.getUplinkToDestination(destinationNode.getId());
            double messageTransferTime = calculateTransferTime(uplink.getLatency(), uplink.getBandwidthBitsPerSecond(), message.getDataPerMessage());
            totalTransferTime += messageTransferTime;
//            System.out.println(String.format("[Calculating total transfer time] %s. Message(%sKByte) from %s(%s) to %s(%s) has a transfer time of %sms",
//                    ++messageCount, message.getDataPerMessage(), sourceNode.getId(), sourceModule.getId(),
//                    destinationNode.getId(), destinationModule.getId(), messageTransferTime));
        }
//        System.out.println(String.format("[Calculating total transfer time] Result: %sms", totalTransferTime));
        return totalTransferTime;
    }

    /**
     * @param latency                in milliseconds
     * @param bandwidthBitsPerSecond in bit/s
     * @param dataSizeKByte          in KByte
     * @return transfer time in milliseconds
     */
    private static double calculateTransferTime(int latency, double bandwidthBitsPerSecond, double dataSizeKByte) {
        double dataSize_bits = dataSizeKByte * 1024 * 8; // KByte -> Byte -> bit
        double bandwidth_bits_per_ms = bandwidthBitsPerSecond / 1000; // bits/s -> bits/ms
//        System.out.println(String.format("Transfer time for %skb with %sMbit/s and latency of %sms: %sms", dataSizeKByte, bandwidthBitsPerSecond / 1000000, latency, (latency + dataSize_bits / bandwidth_bits_per_ms)));
        return latency + dataSize_bits / bandwidth_bits_per_ms;
    }

    public void printDetails() {
        System.out.println("Usage for " + this);
        this.getAllInvolvedFogNodes().forEach(fogNode -> System.out.println(
                String.format("\t[%s]: ramFree:%s; storageFree:%s", fogNode.getId(), fogNode.getRamFree(), fogNode.getStorageFree())
        ));
        this.undeployAllModulesFromNodes();
    }

    private Map<FogNode, List<AppModule>> getNodeToModulesMap() {
        Map<FogNode, List<AppModule>> result = new HashMap<>();
        // TODO
        return result;
    }

    private Set<FogNode> getAllInvolvedFogNodes() {
        return new HashSet<>(this.moduleToNodeMap.values());
    }

    private void undeployAllModulesFromNodes() {
        this.getAllInvolvedFogNodes().forEach(FogNode::undeployAllModules);
    }

    @Override
    public String toString() {
        String moduleDeployments = this.moduleToNodeMap.entrySet().stream().map(mapping -> {
            String moduleId = mapping.getKey().getId();
            String nodeId = mapping.getValue().getId();
            return String.format("%s->%s", moduleId, nodeId);
        }).collect(Collectors.toList()).toString();

        return "AppDeployment{" +
                "application=" + application.getName() +
                ", moduleToNodeMap=" + moduleDeployments +
                ", valid=" + valid +
                ", totalLatency=" + (int) totalLatency + "ms" +
                '}';
    }
}
