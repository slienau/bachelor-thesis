package algorithm.application;

import algorithm.infrastructure.SensorType;

import java.util.*;
import java.util.stream.Collectors;

public class Application {
    private final String name;
    private final int maxLatency;
    private Map<String, AppModule> modules = new HashMap<>();
    private Map<String, AppMessage> messages = new HashMap<>();

    public Application(String name, int maxLatency) {
        this.name = name;
        this.maxLatency = maxLatency;
    }

    private static String messageKey(AppMessage message) {
        return messageKey(message.getSourceModuleId(), message.getDestinationModuleId());
    }

    private static String messageKey(AppSoftwareModule source, AppSoftwareModule destination) {
        return messageKey(source.getId(), destination.getId());
    }

    private static String messageKey(String source, String destination) {
        return String.format("%s->%s", source, destination);
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

    public void addSoftwareModule(String id, int requiredRam, double requiredStorage, int requiredCpuInstructions) {
        this.addSoftwareModule(id, requiredRam, requiredStorage, requiredCpuInstructions, null);
    }

    public void addSoftwareModule(String id, int requiredRam, double requiredStorage, int requiredCpuInstructions, List<SensorType> requiredSensorTypes) {
        AppSoftwareModule newModule = new AppSoftwareModule(id, requiredRam, requiredStorage, requiredCpuInstructions, requiredSensorTypes);
        if (modules.putIfAbsent(id, newModule) != null)
            throw new IllegalArgumentException(String.format("Failed to add %s to %s. Already exists.", id, name));
        System.out.println(String.format("[Application][%s] Added %s", name, newModule));
    }

    public void addMessage(String content, String sourceModule, String destinationModule, double dataPerMessage) {
        if (!this.checkIfModulesExists(sourceModule, destinationModule)) {
            throw new NoSuchElementException(
                    String.format("Failed to add message '%s' (%s->%s) to %s.", content, sourceModule, destinationModule, this.getName()));
        }
        AppMessage message = new AppMessage(content, sourceModule, destinationModule, dataPerMessage);
        if (this.messages.putIfAbsent(messageKey(message), message) != null) {
            throw new IllegalArgumentException(String.format("Failed to add %s to %s. Already exists.", message.getContent(), this.getName()));
        }
        System.out.println(String.format("[Application][%s] Added message '%s' from '%s' to '%s'", this.getName(), message.getContent(), sourceModule, destinationModule));
    }

    public List<AppSoftwareModule> getRequiredSoftwareModules() {
        return this.modules.values().stream()
                .filter(module -> module instanceof AppSoftwareModule)
                .map(module -> (AppSoftwareModule) module)
                .collect(Collectors.toList());
    }

    private AppModule getModuleById(String id) {
        AppModule result = modules.get(id);
        if (result == null)
            throw new NoSuchElementException(String.format("Unable to find module '%s'", id));
        return result;
    }

    private boolean checkIfModulesExists(String... moduleIds) {
        try {
            for (String moduleId : moduleIds)
                this.getModuleById(moduleId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Application{" +
                "name='" + name + '\'' +
                ", maxLatency=" + maxLatency +
                ", modules=" + modules +
                ", messages=" + messages +
                '}';
    }
}
