package algorithm.deployment;

import algorithm.application.Application;
import algorithm.application.ApplicationModule;
import algorithm.entities.FogNode;
import algorithm.infrastructure.Infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Search {
    private final Application a;
    private final Infrastructure i;

    public Search(Application a, Infrastructure i) {
        this.a = a;
        this.i = i;
    }

    public List<Deployment> findAllValidDeployments() {

        Map<ApplicationModule, List<FogNode>> possibleNodesForModule = new HashMap<>();

        for (ApplicationModule module : a.getRequiredModules()) {
            List<FogNode> possibleExecutionNode = new ArrayList<>();
            for (FogNode node : i.getFogNodes()) {
                if (module.getRequiredRam() <= node.getRamTotal() && module.getRequiredStorage() <= node.getStorageTotal()) {
                    possibleExecutionNode.add(node);
                }
            }
            possibleNodesForModule.put(module, possibleExecutionNode);
        }

        possibleNodesForModule.forEach((module, nodeList) -> {
            System.out.println("possible nodes to execute " + module.getId() + ":");
            System.out.println("\t" + nodeList.stream().map(entry -> entry.getId()).collect(Collectors.joining("; ")));
        });

        List<Deployment> possibleDeployments = new ArrayList<>();

        possibleNodesForModule.forEach((module, nodeList) -> {
            nodeList.forEach(fogNode -> {
                fogNode.deployModule(module);
            });
        });


        return null;
    }

    public void getPossibleDeployments() {
        List<ApplicationModule> allModules = a.getRequiredModules();
        List<FogNode> allFogNodes = i.getFogNodes();

        int maxPossibleDeploymentAmount = this.getMaxPossibleDeploymentAmount();
        int modulesAmount = allModules.size();
        int fogNodesAmount = allFogNodes.size();

        int divider = maxPossibleDeploymentAmount / fogNodesAmount;
        System.out.println("divider: " + divider);

        for (FogNode fogNode : allFogNodes) {

        }
    }

    private int getMaxPossibleDeploymentAmount() {
        int allModules = a.getRequiredModules().size();
        int allFogNodes = i.getFogNodes().size();
        double possibleDeployments;
        if (allFogNodes == 0 || allModules == 0)
            possibleDeployments = 0;
        else
            possibleDeployments = Math.pow(allFogNodes, allModules);

        return (int) possibleDeployments;
    }

    public void printInfo() {
        List<ApplicationModule> allModules = a.getRequiredModules();
        List<FogNode> allFogNodes = i.getFogNodes();

        int possibleDeployments = this.getMaxPossibleDeploymentAmount();

        System.out.println(String.format("%s possible (maybe not valid) deployments", possibleDeployments));

        System.out.println(String.format("required modules for application '%s':", this.a.getName()));
        for (ApplicationModule m : allModules) {
            System.out.println("\t" + m);
        }

        System.out.println("available nodes:");
        for (FogNode node : allFogNodes) {
            System.out.println("\t" + node);
        }

    }
}
