package algorithm.infrastructure;

import algorithm.application.AppModule;

import java.util.ArrayList;
import java.util.List;

public class FogNode {
    private final String id;
    private final int ramTotal;
    private final int storageTotal;
    private final int cpuCores;
    private final int cpuInstructionsPerSecond;
    private final List<Sensor> connectedSensors;
    private final List<AppModule> deployedModules;
    private final Infrastructure infrastructure;

    FogNode(String id, int ramTotal, int storageTotal, int cpuCores, int cpuInstructionsPerSecond, Infrastructure infrastructure) {
        this.id = id;
        this.ramTotal = ramTotal;
        this.storageTotal = storageTotal;
        this.cpuCores = cpuCores;
        this.cpuInstructionsPerSecond = cpuInstructionsPerSecond;
        this.connectedSensors = new ArrayList<>();
        this.deployedModules = new ArrayList<>();
        this.infrastructure = infrastructure;
    }

    public NetworkUplink getUplinkToDestination(String destinationId) {
        return this.infrastructure.getUplink(this.getId(), destinationId);
    }

    public String getId() {
        return id;
    }

    public void addSensor(String id, SensorType type) {
        this.connectedSensors.add(new Sensor(id, type));
    }

    public boolean deployModule(AppModule appModule) {
//        System.out.println(String.format("Trying to deploy %s on %s", appModule.getId(), this.getId()));
        if (appModule.getRequiredRam() > this.getRamFree() || appModule.getRequiredStorage() > this.getStorageFree()) {
//            System.out.println(String.format("%s can not be deployed on %s", appModule.getId(), this.getId()));
            return false;
        }

        for (SensorType requiredSensorType : appModule.getRequiredSensorTypes()) {
            if (!this.getConnectedSensorTypes().contains(requiredSensorType)) {
//                System.out.println(String.format("%s can not be deployed to %s because it doesn't contain sensor type %s", appModule.getId(), this.getId(), requiredSensorType));
                return false;
            }
        }

        deployedModules.add(appModule);
//        System.out.println(String.format("%s successfully deployed to %s; freeRam: %s; freeStorage: %s", appModule.getId(), this.getId(), this.getRamFree(), this.getStorageFree()));
        return true;
    }

    public void undeployAllModules() {
        this.deployedModules.clear();
    }

    public int getRamFree() {
        int ramFree = this.ramTotal;
        for (AppModule module : this.deployedModules) {
            ramFree -= module.getRequiredRam();
        }
        return ramFree;
    }

    public int getStorageFree() {
        int storageFree = this.storageTotal;
        for (AppModule module : this.deployedModules) {
            storageFree -= module.getRequiredStorage();
        }
        return storageFree;
    }

    private List<SensorType> getConnectedSensorTypes() {
        List<SensorType> connectedSensorTypes = new ArrayList<>();
        this.connectedSensors.forEach(sensor -> {
            connectedSensorTypes.add(sensor.getType());
        });
        return connectedSensorTypes;
    }

    /**
     * @param module The AppModule to execute on this node
     * @return Processing time for module on this node in milliseconds
     */
    public double calculateProcessingTimeForModule(AppModule module) {
        return (double) module.getRequiredCpuInstructionsPerMessage() / (double) (this.cpuInstructionsPerSecond / 1000); // seconds -> milliseconds
    }

    @Override
    public String toString() {
        return "FogNode{" +
                "id='" + id + '\'' +
                ", ram=" + ramTotal +
                ", storage=" + storageTotal +
                ", cpuCores=" + cpuCores +
                ", cpuScoreSingleCore=" + cpuInstructionsPerSecond +
                '}';
    }
}
