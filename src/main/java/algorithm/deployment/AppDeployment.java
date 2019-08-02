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
    private final Map<String, ModuleDeployment> moduleDeployments; // <ModuleId; Deployment>

    AppDeployment(Application application, List<ModuleDeployment> moduleDeployments) {
        this.application = application;
        this.moduleDeployments = new HashMap<>();
        moduleDeployments.forEach(modDep -> this.moduleDeployments.put(modDep.getModule().getId(), modDep));
    }

    @Override
    public String toString() {
        return "AppDeployment{" +
                "moduleDeployments=" + this.getModuleDeploymentsIdsOnly() +
                '}';
    }

    private List<String> getModuleDeploymentsIdsOnly() {
        return moduleDeployments.values().stream().map(moduleDeployment -> {
            String moduleId = moduleDeployment.getModule().getId();
            String nodeId = moduleDeployment.getNode().getId();
            return String.format("%s->%s", moduleId, nodeId);
        }).collect(Collectors.toList());
    }

    boolean checkValidity() {
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
        for (ModuleDeployment modDep : moduleDeployments.values()) {
            AppModule module = modDep.getModule();
            FogNode node = modDep.getNode();
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

    public double calculateTotalLatency() {
        return calculateTotalTransferTime() + calculateTotalProcessingTime();
    }

    private double calculateTotalProcessingTime() {
        double totalProcessingTime = 0;
        for (ModuleDeployment deployments : moduleDeployments.values()) {
            totalProcessingTime += deployments.getNode().calculateProcessingTimeForModule(deployments.getModule());
        }
        return totalProcessingTime;
    }

    private double calculateTotalTransferTime() {
//        System.out.println(String.format("[Calculating total transfer time] for %s", this));
        double totalTransferTime = 0;
        int messageCount = 0;
        for (AppMessage message : application.getMessages()) {
            AppModule sourceModule = message.getSource();
            AppModule destinationModule = message.getDestination();
            FogNode sourceNode = moduleDeployments.get(sourceModule.getId()).getNode();
            FogNode destinationNode = moduleDeployments.get(destinationModule.getId()).getNode();

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
        double transferTime = latency + dataSize_bits / bandwidth_bits_per_ms;
//        System.out.println(String.format("Transfer time for %skb with %sMbit/s and latency of %sms: %sms", dataSizeKByte, bandwidthMbits, latency, transferTime));
        return transferTime;
    }

    private FogNode getNode(AppModule module) {
        return null;
    }

    public void printUsage() {
        System.out.println("Usage for " + this);
        this.moduleDeployments.values().forEach(moduleDeployment -> moduleDeployment.getNode().deployModule(moduleDeployment.getModule()));
        this.getAllInvolvedFogNodes().forEach(fogNode -> System.out.println(
                String.format("\t[%s]: ramFree:%s; storageFree:%s", fogNode.getId(), fogNode.getRamFree(), fogNode.getStorageFree())
        ));
        this.undeployAllModulesFromNodes();
    }

    private List<FogNode> getAllInvolvedFogNodes() {
        Set<FogNode> result = new HashSet<>();
        this.moduleDeployments.values().forEach(modDep -> result.add(modDep.getNode()));
        return new ArrayList<>(result);
    }

    private void undeployAllModulesFromNodes() {
        this.getAllInvolvedFogNodes().forEach(FogNode::undeployAllModules);
    }
}
