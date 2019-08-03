package algorithm.application;

import java.util.*;
import java.util.stream.Collectors;

public class Application {
    private final String name;
    private Map<String, AppModule> modules = new HashMap<>();
    private Map<String, AppMessage> messages = new HashMap<>();
    private List<AppLoop> loops = new ArrayList<>();

    public Application(String name) {
        this.name = name;
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

    public void addLoop(String loopName, int maxLatency, List<String> modules) {
        this.loops.add(new AppLoop(loopName, maxLatency, modules));
    }

    public List<AppLoop> getLoops() {
        return loops;
    }

    public String getName() {
        return name;
    }

    public List<AppMessage> getMessages() {
        return new ArrayList<>(messages.values());
    }

    public void addSoftwareModule(String id, int requiredRam, double requiredStorage, int requiredCpuInstructions) {
        this.addModule(new AppSoftwareModule(id, requiredRam, requiredStorage, requiredCpuInstructions));
    }

    public void addHardwareModule(String id) {
        this.addModule(new AppHardwareModule(id));
    }

    private void addModule(AppModule newModule) {
        if (modules.putIfAbsent(newModule.getId(), newModule) != null)
            throw new IllegalArgumentException(String.format("Failed to add %s to %s. Already exists.", newModule.getId(), this.getName()));
        System.out.println(String.format("[Application][%s] Added %s", this.getName(), newModule));
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
                ", modules=" + modules +
                ", messages=" + messages +
                '}';
    }
}
