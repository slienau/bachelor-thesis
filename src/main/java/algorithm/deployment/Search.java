package algorithm.deployment;

import algorithm.application.AppModule;
import algorithm.application.Application;
import algorithm.infrastructure.FogNode;
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

    public AppDeployment getFastestDeployment() {
        return Search.getFastestDeployment(this.getValidAppDeployments());
    }

    private List<AppDeployment> getValidAppDeployments() {
        List<AppDeployment> validDeployments = new ArrayList<>();
        for (AppDeployment dep : this.getAppDeploymentsUnchecked()) {
            if (dep.isValid()) {
                System.out.println(String.format("[Search] Found valid %s with latency of %sms", dep, dep.getTotalLatency()));
                validDeployments.add(dep);
            }
        }
        return validDeployments;
    }

    /**
     * Returns a List of all possible combinations of module to node mappings.
     * Deployments are possibly invalid because it's not checked if software, hardware or network requirements are fulfilled.
     *
     * @return
     */
    private List<AppDeployment> getAppDeploymentsUnchecked() {
        List<AppModule> modules = a.getRequiredModules();
        List<FogNode> fogNodes = i.getFogNodes();

        List<List<FogNode>> deploymentsWithoutModule = Generator
                .permutation(fogNodes)
                .withRepetitions(modules.size())
                .stream()
                .collect(Collectors.toList());

        List<AppDeployment> appDeployments = new ArrayList<>();

        deploymentsWithoutModule.forEach(depWithout -> {
            List<ModuleDeployment> moduleDeployments = new ArrayList<>();
            for (int i = 0; i < depWithout.size(); i++) {
                AppModule module = modules.get(i);
                FogNode node = depWithout.get(i);
                ModuleDeployment md = new ModuleDeployment(module, node);
                moduleDeployments.add(md);
            }
            appDeployments.add(new AppDeployment(a, moduleDeployments));
        });

        return appDeployments;
    }

    private static AppDeployment getFastestDeployment(List<AppDeployment> validDeployments) {
        AppDeployment fastestDeployment = null;
        for (AppDeployment dep : validDeployments) {
            double thisLatency = dep.getTotalLatency();
            if (fastestDeployment == null || fastestDeployment.getTotalLatency() > thisLatency)
                fastestDeployment = dep;
        }
        return fastestDeployment;
    }

}
