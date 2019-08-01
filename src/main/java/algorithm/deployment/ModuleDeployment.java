package algorithm.deployment;

import algorithm.application.AppModule;
import algorithm.infrastructure.FogNode;

public class ModuleDeployment {
    private final AppModule module;
    private final FogNode node;

    ModuleDeployment(AppModule module, FogNode node) {
        this.module = module;
        this.node = node;
    }

    AppModule getModule() {
        return module;
    }

    FogNode getNode() {
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
