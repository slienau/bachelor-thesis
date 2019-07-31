package algorithm.deployment;

import java.util.List;
import java.util.stream.Collectors;

public class AppDeployment {
    private final List<ModuleDeployment> moduleDeployments;

    public AppDeployment(List<ModuleDeployment> moduleDeployments) {
        this.moduleDeployments = moduleDeployments;
    }

    public List<ModuleDeployment> getModuleDeployments() {
        return moduleDeployments;
    }

    @Override
    public String toString() {
        return "AppDeployment{" +
                "moduleDeployments=\n\t" + this.getModuleDeploymentsIdsOnly() +
                '}';
    }

    private List<String> getModuleDeploymentsIdsOnly() {
        return moduleDeployments.stream().map(moduleDeployment -> {
            String moduleId = moduleDeployment.getModule().getId();
            String nodeId = moduleDeployment.getNode().getId();
            return String.format("%s->%s", moduleId, nodeId);
        }).collect(Collectors.toList());
    }
}
