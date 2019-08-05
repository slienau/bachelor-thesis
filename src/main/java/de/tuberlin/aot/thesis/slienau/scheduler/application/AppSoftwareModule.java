package de.tuberlin.aot.thesis.slienau.scheduler.application;

import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;

import java.util.ArrayList;
import java.util.List;

public class AppSoftwareModule extends AppModule {
    private final int requiredRam; // MB
    private final double requiredStorage; // GB
    private final int requiredCpuInstructionsPerMessage;
    private final List<String> requiredHardwareModules;

    AppSoftwareModule(String id, String inputType, String outputType, int requiredRam, double requiredStorage, int requiredCpuInstructionsPerMessage, List<String> requiredHardwareModules) {
        super(id, inputType, outputType);
        this.requiredRam = requiredRam;
        this.requiredStorage = requiredStorage;
        this.requiredCpuInstructionsPerMessage = requiredCpuInstructionsPerMessage;
        this.requiredHardwareModules = new ArrayList<>();
        if (requiredHardwareModules != null)
            this.requiredHardwareModules.addAll(requiredHardwareModules);
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

    public List<String> getRequiredHardwareModules() {
        return requiredHardwareModules;
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
                ", requiredHardwareModules=" + requiredHardwareModules +
                '}';
    }
}
