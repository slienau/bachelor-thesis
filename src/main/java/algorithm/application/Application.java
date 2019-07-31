package algorithm.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Application {
    private final String name;
    private final int maxLatency;
    private List<AppModuleConnection> moduleConnections = new ArrayList<>();

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

    public Application addModuleConnection(AppModuleConnection connection) {
        this.moduleConnections.add(connection);
        return this;
    }

    public List<AppModuleConnection> getModuleConnections() {
        return moduleConnections;
    }

    public List<AppModule> getRequiredModules() {
        Set<AppModule> moduleSet = new HashSet<>();
        this.moduleConnections.stream().forEach(connection -> {
            moduleSet.add(connection.getSource());
            moduleSet.add(connection.getDestination());
        });
        return new ArrayList<>(moduleSet);
    }
}
