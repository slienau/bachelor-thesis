package algorithm.application;

import algorithm.infrastructure.FogNode;

public class AppSoftwareModule extends AppModule {
    private final int requiredRam; // MB
    private final double requiredStorage; // GB
    private final int requiredCpuInstructionsPerMessage;

    AppSoftwareModule(String id, int requiredRam, double requiredStorage, int requiredCpuInstructionsPerMessage) {
        super(id);
        this.requiredRam = requiredRam;
        this.requiredStorage = requiredStorage;
        this.requiredCpuInstructionsPerMessage = requiredCpuInstructionsPerMessage;
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
                '}';
    }
}
