package algorithm.infrastructure;

import algorithm.Utils;
import algorithm.application.AppSoftwareModule;

import java.util.*;
import java.util.stream.Collectors;

public class FogNode {
    private final String id;
    private final int ramTotal;
    private final double storageTotal;
    private final int cpuCores;
    private final int cpuInstructionsPerSecond;
    private final List<Sensor> connectedSensors;
    private final List<AppSoftwareModule> deployedModules;
    private final Map<String, NetworkUplink> uplinks; // key: destination node

    FogNode(String id, int ramTotal, int storageTotal, int cpuCores, int cpuInstructionsPerSecond) {
        this.id = id;
        this.ramTotal = ramTotal;
        this.storageTotal = storageTotal;
        this.cpuCores = cpuCores;
        this.cpuInstructionsPerSecond = cpuInstructionsPerSecond;
        this.connectedSensors = new ArrayList<>();
        this.deployedModules = new ArrayList<>();
        this.uplinks = new HashMap<>();
        // add uplink to node itself with 0 latency and unlimited bandwidth
        this.uplinks.put(this.getId(), new NetworkUplink(this, this, 0, Long.MAX_VALUE));
    }

    void addUplink(NetworkUplink uplink) {
        if (this.uplinks.putIfAbsent(uplink.getDestination().getId(), uplink) != null)
            throw new IllegalArgumentException(String.format("Unable to add %s because it already exists.", uplink));
        System.out.println(String.format("[FogNode][%10s] Added %s", this.getId(), uplink));
    }

    public NetworkUplink getUplinkTo(String destinationId) {
        return this.uplinks.get(destinationId);
    }

    public String getId() {
        return id;
    }

    public void addSensor(String id, SensorType type) {
        this.connectedSensors.add(new Sensor(id, type));
    }

    public boolean deployModule(AppSoftwareModule appSoftwareModule) {
//        System.out.println(String.format("Trying to deploy %s on %s", appSoftwareModule.getId(), this.getId()));
        if (appSoftwareModule.getRequiredRam() > this.getRamFree() || appSoftwareModule.getRequiredStorage() > this.getStorageFree()) {
//            System.out.println(String.format("%s can not be deployed on %s", appSoftwareModule.getId(), this.getId()));
            return false;
        }

        for (SensorType requiredSensorType : appSoftwareModule.getRequiredSensorTypes()) {
            if (!this.getConnectedSensorTypes().contains(requiredSensorType)) {
//                System.out.println(String.format("%s can not be deployed to %s because it doesn't contain sensor type %s", appSoftwareModule.getId(), this.getId(), requiredSensorType));
                return false;
            }
        }

        deployedModules.add(appSoftwareModule);
//        System.out.println(String.format("%s successfully deployed to %s; freeRam: %s; freeStorage: %s", appSoftwareModule.getId(), this.getId(), this.getRamFree(), this.getStorageFree()));
        return true;
    }

    public boolean deployModules(List<AppSoftwareModule> modules) {
        boolean result = true;
        for (AppSoftwareModule module : modules) {
            if (!this.deployModule(module)) {
                result = false;
                break;
            }
        }
        if (!result)
            modules.forEach(this::undeployModule);
        return result;
    }

    public void undeployModule(AppSoftwareModule module) {
        this.deployedModules.remove(module);
    }

    public void undeployAllModules() {
        this.deployedModules.clear();
    }

    private int getRamFree() {
        int ramFree = this.ramTotal;
        for (AppSoftwareModule module : this.deployedModules) {
            ramFree -= module.getRequiredRam();
        }
        return ramFree;
    }

    private int getRamUsed() {
        return this.ramTotal - this.getRamFree();
    }

    private double getRamUsedPercent() {
        return Utils.makePercent(this.getRamUsed(), this.ramTotal);
    }

    private double getStorageFree() {
        double storageFree = this.storageTotal;
        for (AppSoftwareModule module : this.deployedModules) {
            storageFree -= module.getRequiredStorage();
        }
        return storageFree;
    }

    private double getStorageUsed() {
        return Utils.round(this.storageTotal - this.getStorageFree());
    }

    private double getStorageUsedPercent() {
        return Utils.makePercent(this.getStorageUsed(), this.storageTotal);
    }

    private Set<SensorType> getConnectedSensorTypes() {
        Set<SensorType> connectedSensorTypes = new HashSet<>();
        this.connectedSensors.forEach(sensor -> connectedSensorTypes.add(sensor.getType()));
        return connectedSensorTypes;
    }

    /**
     * @param module The AppSoftwareModule to execute on this node
     * @return Processing time for module on this node in milliseconds
     */
    public double calculateProcessingTimeForModule(AppSoftwareModule module) {
        double instructionsPerMessage = module.getRequiredCpuInstructionsPerMessage();
        double cpuInstructionsPerSecond = this.cpuInstructionsPerSecond;
        return Utils.round((instructionsPerMessage / cpuInstructionsPerSecond) * 1000);
    }

    public String getProcessingTimeString(AppSoftwareModule module) {
        String processingStringTemplate = "%6sms Processing time for module '%s' on '%s'.";
        double processingTime = this.calculateProcessingTimeForModule(module);
        return String.format(processingStringTemplate, processingTime, module.getId(), this.getId());
    }

    @Override
    public String toString() {
        return "FogNode{" +
                "id='" + id + '\'' +
                String.format(", ramUsed=%sMB/%sMB (%s%%)", this.getRamUsed(), this.ramTotal, this.getRamUsedPercent()) +
                String.format(", storageUsed=%sGB/%sGB (%s%%)", this.getStorageUsed(), this.storageTotal, this.getStorageUsedPercent()) +
                ", cpuCores=" + cpuCores +
                ", cpuInstructionsPerSecond=" + cpuInstructionsPerSecond +
                ", connectedSensors=[" + connectedSensors.stream().map(Sensor::getId).collect(Collectors.joining(", ")) + "]" +
                ", deployedModules=[" + deployedModules.stream().map(module -> String.format("%s (RAM %sMB/Storage %sGB)", module.getId(), module.getRequiredRam(), module.getRequiredStorage())).collect(Collectors.joining(", ")) + "]" +
                ", uplinks=[" + uplinks.values().stream().map(uplink -> String.format("{to: %s, %sms, %sMbit/s}", uplink.getDestination().getId(), uplink.getLatency(), uplink.getMBitPerSecond())).collect(Collectors.joining(", ")) + "]" +
                '}';
    }
}
