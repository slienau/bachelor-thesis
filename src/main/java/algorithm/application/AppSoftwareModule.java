package algorithm.application;

import algorithm.infrastructure.FogNode;
import algorithm.infrastructure.SensorType;

import java.util.ArrayList;
import java.util.List;

public class AppSoftwareModule extends AppModule {
    private final int requiredRam; // MB
    private final double requiredStorage; // GB
    private final int requiredCpuInstructionsPerMessage;
    private final List<SensorType> requiredSensorTypes = new ArrayList<>();

    public AppSoftwareModule(String id, int requiredRam, double requiredStorage, int requiredCpuInstructionsPerMessage) {
        this(id, requiredRam, requiredStorage, requiredCpuInstructionsPerMessage, null);
    }

    AppSoftwareModule(String id, int requiredRam, double requiredStorage, int requiredCpuInstructionsPerMessage, List<SensorType> requiredSensorTypes) {
        super(id);
        this.requiredRam = requiredRam;
        this.requiredStorage = requiredStorage;
        this.requiredCpuInstructionsPerMessage = requiredCpuInstructionsPerMessage;
        // TODO: Remove requiredSensorTypes from this class -> they can be found by the app loop
        if (requiredSensorTypes != null)
            this.requiredSensorTypes.addAll(requiredSensorTypes);
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

    public String getProcessingTimeString(FogNode node) {
        return node.getProcessingTimeString(this);
    }

    @Override
    public String toString() {
        return "AppSoftwareModule{" +
                "id='" + super.getId() + '\'' +
                ", requiredRam=" + requiredRam +
                ", requiredStorage=" + requiredStorage +
                ", requiredCpuInstructions=" + requiredCpuInstructionsPerMessage +
                ", requiredSensorTypes=" + requiredSensorTypes +
                '}';
    }
}
