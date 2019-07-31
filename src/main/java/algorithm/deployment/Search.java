package algorithm.deployment;

import algorithm.application.Application;
import algorithm.application.ApplicationModule;
import algorithm.entities.FogNode;
import algorithm.infrastructure.Infrastructure;
import org.paukov.combinatorics3.Generator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Search {
    private final Application a;
    private final Infrastructure i;

    public Search(Application a, Infrastructure i) {
        this.a = a;
        this.i = i;
    }

    /**
     * Returns a List of all possible combinations of module to node mappings.
     * Deployments are possibly invalid because it's not checked if software, hardware or network requirements are fulfilled.
     *
     * @return
     */
    public List<AppDeployment> getAppDeploymentsUnchecked() {
        List<ApplicationModule> modules = a.getRequiredModules();
        List<FogNode> fogNodes = i.getFogNodes();

        List<List<FogNode>> deploymentsWithoutModule = Generator
                .permutation(fogNodes)
                .withRepetitions(modules.size())
                .stream()
                .collect(Collectors.toList());

        List<AppDeployment> appDeployments = new ArrayList<>();

        deploymentsWithoutModule.stream().forEach(depWithout -> {
            List<ModuleDeployment> moduleDeployments = new ArrayList<>();
            for (int i = 0; i < depWithout.size(); i++) {
                ApplicationModule module = modules.get(i);
                FogNode node = depWithout.get(i);
                ModuleDeployment md = new ModuleDeployment(module, node);
                moduleDeployments.add(md);
            }
            appDeployments.add(new AppDeployment(moduleDeployments));
        });

        return appDeployments;
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
