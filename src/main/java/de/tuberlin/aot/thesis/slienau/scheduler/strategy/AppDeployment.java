package de.tuberlin.aot.thesis.slienau.scheduler.strategy;

import de.tuberlin.aot.thesis.slienau.scheduler.application.AppLoop;
import de.tuberlin.aot.thesis.slienau.scheduler.application.AppSoftwareModule;
import de.tuberlin.aot.thesis.slienau.scheduler.application.Application;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;

import java.util.*;
import java.util.stream.Collectors;

public class AppDeployment {
    private final Application application;
    private final Map<AppSoftwareModule, FogNode> moduleToNodeMap;
    private final boolean valid;

    AppDeployment(Application application, Map<AppSoftwareModule, FogNode> moduleToNodeMap) {
        this.application = application;
        this.moduleToNodeMap = moduleToNodeMap;
        this.valid = this.checkValidity();
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * Returns the destination fog nodes (destinations for the output AppMessage) for a given source module (deployed on another/same fog node)
     *
     * @param sourceModuleId
     * @return
     */
    public List<String> getDestinationNodesForSourceModule(String sourceModuleId) {
        List<String> destinations = new ArrayList<>();
        application.getLoops().forEach(loop -> {
            String destinationNodeId = loop.getDestinationNodeForSourceModule(sourceModuleId, this);
            if (destinationNodeId != null)
                destinations.add(destinationNodeId);
        });
        return destinations;
    }

    private boolean checkValidity() {
//        System.out.println(String.format("[AppDeployment] Validating %s", this));
        if (!this.validateHardwareRequirements())
            return false; // no further checking if hardware requirement check already failed
        if (!this.validateLatencyRequirements())
            return false;
//        System.out.println(String.format("[AppDeployment] Valid %s", this));
        return true;
    }

    private boolean validateHardwareRequirements() {
        boolean valid = true;
        // validate hardware requirements
        for (Map.Entry<AppSoftwareModule, FogNode> entry : moduleToNodeMap.entrySet()) {
            AppSoftwareModule module = entry.getKey();
            FogNode node = entry.getValue();
            if (!node.deployModule(module)) {
                valid = false;
                break;
            }
        }
        // Undeploy all modules from all nodes to free resources for further hardware validity checks
        this.undeployAllModulesFromNodes();

//        if (valid)
//            System.out.println(String.format("[AppDeployment] [Hardware Requirement Check] SUCCESS for %s", this));
//        else
//            System.out.println(String.format("[AppDeployment] [Hardware Requirement Check] FAILED for %s", this));

        return valid;
    }

    private boolean validateLatencyRequirements() {
        boolean valid = true;
        for (AppLoop loop : this.application.getLoops()) {
            valid = loop.hasValidLatencyRequirements(this);
            if (!valid)
                break;
        }
        return valid;
    }

    /**
     * Gets latency for every AppLoop and returns max.
     *
     * @return
     */
    public double getMaxLoopLatency() {
        return getMaxLoopTransferTime() + getMaxLoopProcessingTime();
    }

    /**
     * Gets processing time for every AppLoop and returns max.
     *
     * @return
     */
    private double getMaxLoopProcessingTime() {
        double result = 0.0;
        for (AppLoop loop : this.application.getLoops()) {
            double loopProcessingTime = loop.calculateTotalProcessingTime(this);
            if (result < loopProcessingTime)
                result = loopProcessingTime;
        }
        return result;
    }

    /**
     * Gets loop transfer time for every AppLoop and returns max.
     *
     * @return
     */
    private double getMaxLoopTransferTime() {
        double result = 0.0;
        for (AppLoop loop : this.application.getLoops()) {
            double loopTransferTime = loop.calculateTotalTransferTime(this);
            if (result < loopTransferTime)
                result = loopTransferTime;
        }
        return result;
    }

    public String createDetailsString() {
        StringBuilder sb = new StringBuilder()
                .append("**************************************************************\n")
                .append("****** Details for " + this + "\n")
                .append("**************************************************************\n");

        for (AppLoop loop : application.getLoops()) {
            sb.append(loop.createDetailString(this));
        }
        sb
                .append(createSoftwareModuleInfoString())
                .append(createFogNodeUsageString());
        return sb.toString();
    }

    private String createSoftwareModuleInfoString() {
        StringBuilder sb = new StringBuilder()
                .append("----------------------\n")
                .append("Module deployment info\n")
                .append("----------------------\n");
        for (AppSoftwareModule module : application.getRequiredSoftwareModules()) {
            String fogNodeId = this.getNodeForSoftwareModule(module).getId();

            sb.append(String.format("Module '%s' should be deployed on node '%s'. Output message destination nodes: %s",
                    module.getId(), fogNodeId, getDestinationNodesForSourceModule(module.getId())))
                    .append("\n");
        }
        return sb.toString();

    }

    private String createFogNodeUsageString() {
        StringBuilder sb = new StringBuilder()
                .append("----------------\n")
                .append("Fog node usage\n")
                .append("----------------\n");
        this.getNodeToModulesMap().forEach(FogNode::deployModules);
        this.getAllInvolvedFogNodes().forEach(node -> sb.append(node).append("\n"));
        this.undeployAllModulesFromNodes();
        return sb.toString();
    }

    private Map<FogNode, List<AppSoftwareModule>> getNodeToModulesMap() {
        Map<FogNode, List<AppSoftwareModule>> result = new HashMap<>();
        // initialize Map with keys and empty list
        this.getAllInvolvedFogNodes().forEach(fogNode -> result.put(fogNode, new ArrayList<>()));
        // add modules to list (values)
        this.moduleToNodeMap.forEach((module, node) -> result.get(node).add(module));
        return result;
    }

    private Set<FogNode> getAllInvolvedFogNodes() {
        return new HashSet<>(this.moduleToNodeMap.values());
    }

    private void undeployAllModulesFromNodes() {
        this.getAllInvolvedFogNodes().forEach(FogNode::undeployAllModules);
    }

    public Application getApplication() {
        return application;
    }

    public Map<AppSoftwareModule, FogNode> getModuleToNodeMap() {
        return moduleToNodeMap;
    }

    public FogNode getNodeForSoftwareModule(String softwareModuleId) {
        return this.getNodeForSoftwareModule(application.getSoftwareModuleById(softwareModuleId));
    }

    public FogNode getNodeForSoftwareModule(AppSoftwareModule softwareModule) {
        return this.moduleToNodeMap.get(softwareModule);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppDeployment that = (AppDeployment) o;
        return valid == that.valid &&
                application.equals(that.application) &&
                moduleToNodeMap.equals(that.moduleToNodeMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, moduleToNodeMap, valid);
    }

    @Override
    public String toString() {
        String moduleDeployments = this.moduleToNodeMap.entrySet().stream().map(mapping -> {
            String moduleId = mapping.getKey().getId();
            String nodeId = mapping.getValue().getId();
            return String.format("%s->%s", moduleId, nodeId);
        }).collect(Collectors.toList()).toString();

        return "AppDeployment{" +
                "application=" + application.getName() +
                ", moduleToNodeMap=" + moduleDeployments +
                ", valid=" + valid +
                ", maxLatency=" + (int) this.getMaxLoopLatency() + "ms" +
                '}';
    }
}
