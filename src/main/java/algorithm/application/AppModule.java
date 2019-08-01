package algorithm.application;

import algorithm.infrastructure.SensorType;

import java.util.ArrayList;
import java.util.List;

public class AppModule {
    private final String id;
    private final int requiredRam; // MB
    private final double requiredStorage; // GB
    private final List<SensorType> requiredSensorTypes = new ArrayList<>();

    public AppModule(String id, int requiredRam, double requiredStorage) {
        this(id, requiredRam, requiredStorage, null);
    }

    AppModule(String id, int requiredRam, double requiredStorage, List<SensorType> requiredSensorTypes) {
        this.id = id;
        this.requiredRam = requiredRam;
        this.requiredStorage = requiredStorage;
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

    public List<SensorType> getRequiredSensorTypes() {
        return requiredSensorTypes;
    }

    @Override
    public String toString() {
        return "AppModule{" +
                "id='" + id + '\'' +
                ", requiredRam=" + requiredRam +
                ", requiredStorage=" + requiredStorage +
                '}';
    }
}
