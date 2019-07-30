package algorithm.entities;

public class Sensor {
    private final String id;
    private final SensorType type;

    public Sensor(String id, SensorType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public SensorType getType() {
        return type;
    }
}
