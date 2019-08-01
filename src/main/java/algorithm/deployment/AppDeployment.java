package algorithm.deployment;

import algorithm.application.AppModule;
import algorithm.infrastructure.FogNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AppDeployment {
    private final List<ModuleDeployment> moduleDeployments;

    AppDeployment(List<ModuleDeployment> moduleDeployments) {
        this.moduleDeployments = moduleDeployments;
    }

    public List<ModuleDeployment> getModuleDeployments() {
        return moduleDeployments;
    }

    @Override
    public String toString() {
        return "AppDeployment{" +
                "moduleDeployments=" + this.getModuleDeploymentsIdsOnly() +
                '}';
    }

    private List<String> getModuleDeploymentsIdsOnly() {
        return moduleDeployments.stream().map(moduleDeployment -> {
            String moduleId = moduleDeployment.getModule().getId();
            String nodeId = moduleDeployment.getNode().getId();
            return String.format("%s->%s", moduleId, nodeId);
        }).collect(Collectors.toList());
    }

    boolean checkValidity() {
        boolean valid = true;
        for (ModuleDeployment modDep : moduleDeployments) {
            AppModule module = modDep.getModule();
            FogNode node = modDep.getNode();
            if (!node.deployModule(module)) {
                valid = false;
                break;
            }
        }
        // Undeploy all modules from all nodes
        this.undeployAllModulesFromNodes();

        return valid;
    }

    public void printUsage() {
        System.out.println("Usage for " + this);
        this.moduleDeployments.forEach(moduleDeployment -> moduleDeployment.getNode().deployModule(moduleDeployment.getModule()));
        this.getAllInvolvedFogNodes().forEach(fogNode -> System.out.println(
                String.format("\t[%s]: ramFree:%s; storageFree:%s", fogNode.getId(), fogNode.getRamFree(), fogNode.getStorageFree())
        ));
        this.undeployAllModulesFromNodes();
    }

    private List<FogNode> getAllInvolvedFogNodes() {
        Set<FogNode> result = new HashSet<>();
        this.moduleDeployments.forEach(modDep -> result.add(modDep.getNode()));
        return new ArrayList<>(result);
    }

    private void undeployAllModulesFromNodes() {
        this.getAllInvolvedFogNodes().forEach(FogNode::undeployAllModules);
    }
}
