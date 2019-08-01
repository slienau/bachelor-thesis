package algorithm.application;

import algorithm.entities.SensorType;

import java.util.ArrayList;
import java.util.List;

public class AppModule {
    private final String id;
    private final int requiredRam; // MB
    private final double requiredStorage; // GB
    private final List<SensorType> requiredSensorTypes;

    public AppModule(String id, int requiredRam, double requiredStorage) {
        this(id, requiredRam, requiredStorage, new ArrayList<>());
    }

    public AppModule(String id, int requiredRam, double requiredStorage, List<SensorType> requiredSensorTypes) {
        this.id = id;
        this.requiredRam = requiredRam;
        this.requiredStorage = requiredStorage;
        this.requiredSensorTypes = requiredSensorTypes;
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

    public void addRequiredSensorType(SensorType sensorType) {
        requiredSensorTypes.add(sensorType);
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
