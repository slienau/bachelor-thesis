package de.tuberlin.aot.thesis.slienau.scheduler.infrastructure;

import de.tuberlin.aot.thesis.slienau.scheduler.application.AppSoftwareModule;
import de.tuberlin.aot.thesis.slienau.utils.SchedulerUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FogNode {
    private final String id;
    private final Set<String> connectedHardware = new HashSet<>();
    private final List<AppSoftwareModule> deployedModules = new ArrayList<>();
    private final Map<String, NetworkUplink> uplinks = new HashMap<>(); // key: destination node
    private float ramTotal;
    private float storageTotal;
    private int cpuCores;
    private int cpuInstructionsPerSecond;

    public FogNode(String id, List<String> connectedHardware) {
        this.id = id;
        if (connectedHardware != null)
            this.connectedHardware.addAll(connectedHardware);
        this.addUplinkToSelf();
    }

    public FogNode(String id, float ramTotal, float storageTotal, int cpuCores, int cpuInstructionsPerSecond, List<String> connectedHardware) {
        this.id = id;
        this.ramTotal = ramTotal;
        this.storageTotal = storageTotal;
        this.cpuCores = cpuCores;
        this.cpuInstructionsPerSecond = cpuInstructionsPerSecond;
        if (connectedHardware != null)
            this.connectedHardware.addAll(connectedHardware);
        // add uplink to node itself with 0 latency and unlimited bandwidth
        this.addUplinkToSelf();
    }

    private void addUplinkToSelf() {
        this.uplinks.put(this.getId(), new NetworkUplink(this, this, 0, Long.MAX_VALUE));
    }

    public void addUplink(NetworkUplink uplink) {
        if (this.uplinks.putIfAbsent(uplink.getDestination().getId(), uplink) != null)
            throw new IllegalArgumentException(String.format("Unable to add %s because it already exists.", uplink));
        System.out.println(String.format("[FogNode][%10s] Added %s", this.getId(), uplink));
    }

    public NetworkUplink getUplinkTo(String destinationId) {
        return this.uplinks.get(destinationId);
    }

    public boolean removeUplinkTo(String destinationId) {
        System.out.println(String.format("[FogNode][%s] Removing uplink to %s", this.getId(), destinationId));
        return this.uplinks.remove(destinationId) != null;
    }

    public String getId() {
        return id;
    }

    public boolean deployModule(AppSoftwareModule appSoftwareModule) {
//        System.out.println(String.format("Trying to deploy %s on %s", appSoftwareModule.getId(), this.getId()));
        if (appSoftwareModule.getRequiredRam() > this.getRamFree() || appSoftwareModule.getRequiredStorage() > this.getStorageFree()) {
//            System.out.println(String.format("%s can not be deployed on %s because of missing hardware requirements", appSoftwareModule.getId(), this.getId()));
            return false;
        }

        for (String requiredHardwareModule : appSoftwareModule.getRequiredHardwareModules())
            if (!this.getConnectedHardware().contains(requiredHardwareModule)) {
//                System.out.println(String.format("%s can not be deployed on %s because it doesn't fulfil hardware module requirements ('%s' not connected)", appSoftwareModule.getId(), this.getId(), requiredHardwareModule));
                return false;
            }


        deployedModules.add(appSoftwareModule);
//        System.out.println(String.format("%s successfully deployed to %s; freeRam: %s; freeStorage: %s", appSoftwareModule.getId(), this.getId(), this.getRamFree(), this.getStorageFree()));
        return true;
    }

    public Set<String> getConnectedHardware() {
        return this.connectedHardware;
    }

    public void addConnectedHardware(List<String> newHardware) {
        this.connectedHardware.addAll(newHardware);
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

    private float getRamFree() {
        float ramFree = this.ramTotal;
        for (AppSoftwareModule module : this.deployedModules) {
            ramFree -= module.getRequiredRam();
        }
        return ramFree;
    }

    private float getRamUsed() {
        return this.ramTotal - this.getRamFree();
    }

    private double getRamUsedPercent() {
        return SchedulerUtils.makePercent(this.getRamUsed(), this.ramTotal);
    }

    private double getStorageFree() {
        double storageFree = this.storageTotal;
        for (AppSoftwareModule module : this.deployedModules) {
            storageFree -= module.getRequiredStorage();
        }
        return storageFree;
    }

    private double getStorageUsed() {
        return SchedulerUtils.round(this.storageTotal - this.getStorageFree());
    }

    private double getStorageUsedPercent() {
        return SchedulerUtils.makePercent(this.getStorageUsed(), this.storageTotal);
    }

    public void setRamTotal(float ramTotal) {
        this.ramTotal = ramTotal;
    }

    public void setStorageTotal(float storageTotal) {
        this.storageTotal = storageTotal;
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public void setCpuInstructionsPerSecond(int cpuInstructionsPerSecond) {
        this.cpuInstructionsPerSecond = cpuInstructionsPerSecond;
    }

    /**
     * @param module The AppSoftwareModule to execute on this node
     * @return Processing time for module on this node in milliseconds
     */
    public double calculateProcessingTimeForModule(AppSoftwareModule module) {
        double instructionsPerMessage = module.getRequiredCpuInstructionsPerMessage();
        double cpuInstructionsPerSecond = this.cpuInstructionsPerSecond;
        return SchedulerUtils.round((instructionsPerMessage / cpuInstructionsPerSecond) * 1000);
    }

    public String getProcessingTimeString(AppSoftwareModule module) {
        String processingStringTemplate = "%6sms Processing time for module '%s' on '%s'.";
        double processingTime = this.calculateProcessingTimeForModule(module);
        return String.format(processingStringTemplate, processingTime, module.getId(), this.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FogNode fogNode = (FogNode) o;
        return id.equals(fogNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FogNode{" +
                "id='" + id + '\'' +
                String.format(", ramUsed=%sMB/%sMB (%s%%)", this.getRamUsed(), this.ramTotal, this.getRamUsedPercent()) +
                String.format(", storageUsed=%sGB/%sGB (%s%%)", this.getStorageUsed(), this.storageTotal, this.getStorageUsedPercent()) +
                ", cpuCores=" + cpuCores +
                ", cpuInstructionsPerSecond=" + cpuInstructionsPerSecond +
                ", connectedHardware=" + connectedHardware +
                ", deployedModules=[" + deployedModules.stream().map(module -> String.format("%s (RAM %sMB/Storage %sGB)", module.getId(), module.getRequiredRam(), module.getRequiredStorage())).collect(Collectors.joining(", ")) + "]" +
                ", uplinks=[" + uplinks.values().stream().map(uplink -> String.format("{to: %s, %sms, %sMbit/s}", uplink.getDestination().getId(), uplink.getLatency(), uplink.getMBitPerSecond())).collect(Collectors.joining(", ")) + "]" +
                '}';
    }
}
