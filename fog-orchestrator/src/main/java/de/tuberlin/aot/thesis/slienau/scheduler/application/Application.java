package de.tuberlin.aot.thesis.slienau.scheduler.application;

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

    public void addLoop(String loopName, int maxLatency, List<String> modules, boolean allowTypeMismatch) {
        AppLoop newLoop = new AppLoop(loopName, maxLatency, modules, this, allowTypeMismatch);
        this.loops.add(newLoop);
        System.out.println(String.format("[Application][%s] Added %s", this.getName(), newLoop));
    }

    public void addLoop(String loopName, int maxLatency, List<String> modules) {
        addLoop(loopName, maxLatency, modules, false);
    }

    public List<AppLoop> getLoops() {
        return loops;
    }

    public String getName() {
        return name;
    }

    public AppMessage getMessage(String contentType) {
        return this.messages.get(contentType);
    }

    public void addSoftwareModule(String id, String inputType, String outputType, int requiredRam, double requiredStorage, int requiredMi, List<String> requiredHardwareModules) {
        this.addModule(new AppSoftwareModule(id, inputType, outputType, requiredRam, requiredStorage, requiredMi, requiredHardwareModules));
    }

    public void addHardwareModule(String id, String outputType) {
        this.addModule(new AppHardwareModule(id, outputType));
    }

    private void addModule(AppModule newModule) {
        if (modules.putIfAbsent(newModule.getId(), newModule) != null)
            throw new IllegalArgumentException(String.format("Failed to add %s to %s. Already exists.", newModule.getId(), this.getName()));
        System.out.println(String.format("[Application][%s] Added %s", this.getName(), newModule));
    }

    /**
     * @param contentType    Message content (input/output type of an AppModule)
     * @param dataPerMessage in KByte
     */
    public void addMessage(String contentType, double dataPerMessage) {
        AppMessage message = new AppMessage(contentType, dataPerMessage);
        if (this.messages.putIfAbsent(contentType, message) != null) {
            throw new IllegalArgumentException(String.format("[Application][%s] Failed to add %s. Already exists.", this.getName(), message.getContentType()));
        }
        System.out.println(String.format("[Application][%s] Added message with contentType '%s'", this.getName(), message.getContentType()));
    }

    public List<AppSoftwareModule> getRequiredSoftwareModules() {
        return this.modules.values().stream()
                .filter(module -> module instanceof AppSoftwareModule)
                .map(module -> (AppSoftwareModule) module)
                .collect(Collectors.toList());
    }

    public AppSoftwareModule getSoftwareModuleById(String id) {
        AppModule result = this.getModuleById(id);
        if (result instanceof AppSoftwareModule)
            return (AppSoftwareModule) result;
        throw new NoSuchElementException(String.format("[Application][%s] Found module '%s' but it is not of type 'AppSoftwareModule'", this.getName(), id));
    }

    AppModule getModuleById(String id) {
        AppModule result = modules.get(id);
        if (result == null)
            throw new NoSuchElementException(String.format("[Application][%s] Unable to find module '%s'", this.getName(), id));
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
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
