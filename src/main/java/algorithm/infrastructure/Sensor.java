package algorithm.infrastructure;

public class Sensor {
    private final String id;
    private final SensorType type;

    Sensor(String id, SensorType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    SensorType getType() {
        return type;
    }
}
