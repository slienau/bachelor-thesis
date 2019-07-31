package algorithm.deployment;

import algorithm.application.AppModule;
import algorithm.entities.FogNode;

public class ModuleDeployment {
    private final AppModule module;
    private final FogNode node;

    public ModuleDeployment(AppModule module, FogNode node) {
        this.module = module;
        this.node = node;
    }

    public AppModule getModule() {
        return module;
    }

    public FogNode getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "ModuleDeployment{" +
                "module=" + module.getId() +
                " -> node=" + node.getId() +
                '}';
    }
}
