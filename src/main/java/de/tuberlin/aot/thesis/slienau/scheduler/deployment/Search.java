package de.tuberlin.aot.thesis.slienau.scheduler.deployment;

import de.tuberlin.aot.thesis.slienau.scheduler.application.AppSoftwareModule;
import de.tuberlin.aot.thesis.slienau.scheduler.application.Application;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.Infrastructure;
import org.paukov.combinatorics3.Generator;

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

    private static AppDeployment getFastestDeployment(List<AppDeployment> validDeployments) {
        AppDeployment fastestDeployment = null;
        for (AppDeployment dep : validDeployments) {
            double thisLatency = dep.getMaxLoopLatency();
            if (fastestDeployment == null || fastestDeployment.getMaxLoopLatency() > thisLatency)
                fastestDeployment = dep;
        }
        return fastestDeployment;
    }

    public AppDeployment getFastestDeployment() {
        return Search.getFastestDeployment(this.getValidAppDeployments());
    }

    private List<AppDeployment> getValidAppDeployments() {
        List<AppDeployment> validDeployments = new ArrayList<>();
        for (AppDeployment dep : this.getAppLoopDeploymentsUnchecked()) {
            if (dep.isValid()) {
                System.out.println(String.format("[Search] Found valid %s", dep));
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
    private List<AppDeployment> getAppLoopDeploymentsUnchecked() {
        List<AppSoftwareModule> modules = a.getRequiredSoftwareModules();
        List<FogNode> fogNodes = i.getFogNodes();

        List<List<FogNode>> deploymentsWithoutModule = Generator
                .permutation(fogNodes)
                .withRepetitions(modules.size())
                .stream()
                .collect(Collectors.toList());

        List<AppDeployment> appDeployments = new ArrayList<>();

        deploymentsWithoutModule.forEach(depWithout -> {
            Map<AppSoftwareModule, FogNode> moduleToNodeMap = new HashMap<>();
            for (int i = 0; i < depWithout.size(); i++) {
                AppSoftwareModule module = modules.get(i);
                FogNode node = depWithout.get(i);
                moduleToNodeMap.put(module, node);
            }
            appDeployments.add(new AppDeployment(a, moduleToNodeMap));
        });

        return appDeployments;
    }

}
