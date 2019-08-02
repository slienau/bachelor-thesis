package algorithm.application;

import algorithm.infrastructure.FogNode;
import algorithm.infrastructure.SensorType;

import java.util.ArrayList;
import java.util.List;

public class AppModule {
    private final String id;
    private final int requiredRam; // MB
    private final double requiredStorage; // GB
    private final int requiredCpuInstructionsPerMessage;
    private final List<SensorType> requiredSensorTypes = new ArrayList<>();

    public AppModule(String id, int requiredRam, double requiredStorage, int requiredCpuInstructionsPerMessage) {
        this(id, requiredRam, requiredStorage, requiredCpuInstructionsPerMessage, null);
    }

    AppModule(String id, int requiredRam, double requiredStorage, int requiredCpuInstructionsPerMessage, List<SensorType> requiredSensorTypes) {
        this.id = id;
        this.requiredRam = requiredRam;
        this.requiredStorage = requiredStorage;
        this.requiredCpuInstructionsPerMessage = requiredCpuInstructionsPerMessage;
        if (requiredSensorTypes != null)
            this.requiredSensorTypes.addAll(requiredSensorTypes);
    }

    public String getId() {
        return id;
    }

    public int getRequiredRam() {
        return requiredRam;
    }

    public double getRequiredStorage() {
        return requiredStorage;
    }

    public int getRequiredCpuInstructionsPerMessage() {
        return requiredCpuInstructionsPerMessage;
    }

    public List<SensorType> getRequiredSensorTypes() {
        return requiredSensorTypes;
    }

    public String createProcessingTimeString(FogNode node) {
        return node.createProcessingTimeString(this);
    }

    @Override
    public String toString() {
        return "AppModule{" +
                "id='" + id + '\'' +
                ", requiredRam=" + requiredRam +
                ", requiredStorage=" + requiredStorage +
                ", requiredCpuInstructions=" + requiredCpuInstructionsPerMessage +
                ", requiredSensorTypes=" + requiredSensorTypes +
                '}';
    }
}
