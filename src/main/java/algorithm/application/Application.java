package algorithm.application;

import algorithm.infrastructure.SensorType;

import java.util.*;

public class Application {
    private final String name;
    private final int maxLatency;
    private Map<String, AppModule> modules = new HashMap<>();
    private Map<String, AppMessage> messages = new HashMap<>();

    public Application(String name, int maxLatency) {
        this.name = name;
        this.maxLatency = maxLatency;
    }

    public String getName() {
        return name;
    }

    public int getMaxLatency() {
        return maxLatency;
    }

    public List<AppMessage> getMessages() {
        return new ArrayList<>(messages.values());
    }

    public void addModule(String id, int requiredRam, double requiredStorage) {
        this.addModule(id, requiredRam, requiredStorage, null);
    }

    public void addModule(String id, int requiredRam, double requiredStorage, List<SensorType> requiredSensorTypes) {
        AppModule newModule = new AppModule(id, requiredRam, requiredStorage, requiredSensorTypes);
        if (modules.putIfAbsent(id, newModule) != null)
            throw new IllegalArgumentException(String.format("Failed to add %s to %s. Already exists.", id, name));
        System.out.println(String.format("[Application:%s] Added module '%s'", name, id));
    }

    public void addMessage(String content, String sourceModule, String destinationModule, double dataPerMessage) {
        AppModule source = modules.get(sourceModule);
        AppModule destination = modules.get(destinationModule);
        AppMessage message = new AppMessage(content, source, destination, dataPerMessage);
        if (this.messages.putIfAbsent(messageKey(message), message) != null) {
            throw new IllegalArgumentException(String.format("Failed to add %s to %s. Already exists.", message.getContent(), this.getName()));
        }
        System.out.println(String.format("[Application:%s] Added message '%s' from '%s' to '%s'", this.getName(), message.getContent(), sourceModule, destinationModule));
    }

    public List<AppModule> getRequiredModules() {
        return new ArrayList<>(this.modules.values());
    }

    private static String messageKey(AppMessage message) {
        return String.format("%s->%s", message.getSource(), message.getDestination());
    }

    @Override
    public String toString() {
        return "Application{" +
                "name='" + name + '\'' +
                ", maxLatency=" + maxLatency +
                ", moduleConnections=" + messages +
                '}';
    }
}
