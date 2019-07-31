package algorithm.deployment;

import algorithm.application.ApplicationModule;
import algorithm.entities.FogNode;

public class ModuleDeployment {
    private final ApplicationModule module;
    private final FogNode node;

    public ModuleDeployment(ApplicationModule module, FogNode node) {
        this.module = module;
        this.node = node;
    }

    public ApplicationModule getModule() {
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
