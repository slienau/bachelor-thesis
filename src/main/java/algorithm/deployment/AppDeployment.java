package algorithm.deployment;

import algorithm.application.AppLoop;
import algorithm.application.AppSoftwareModule;
import algorithm.application.Application;
import algorithm.infrastructure.FogNode;

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
            double loopProcessingTime = loop.getProcessingTime(this);
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
            double loopTransferTime = loop.getTransferTime(this);
            if (result < loopTransferTime)
                result = loopTransferTime;
        }
        return result;
    }

    public String createDetailsString() {
        StringBuilder sb = new StringBuilder()
                .append("**************************************************************\n")
                .append("****** Details for " + this + "\n")
                .append("**************************************************************");

        sb.append(createStepsString());
        sb.append(createFogNodeUsageString());

        return sb.toString();
    }

    private String createStepsString() {
        // TODO: create string for every apploop (implement in AppLoop and call from here for every apploop)
        /*
        Function<Integer, String> delimiterString = (stepIn) -> String.format("\n%2s.\t", stepIn);

        StringBuilder sb = new StringBuilder();
        int step = 0;
        int iteration = 0;
        for (AppMessage message : this.application.getMessages()) {
            String sourceModule = message.getSourceModuleId();
            FogNode sourceNode = this.moduleToNodeMap.get(sourceModule);
            String destinationModule = message.getDestinationModuleId();
            FogNode destinationNode = this.moduleToNodeMap.get(destinationModule);

            // Print sensor types if needed
            if (sourceModule.getRequiredSensorTypes().size() > 0) {
                String sensorString = String.format(
                        "Module '%s' has required sensors '%s' which are connected to node '%s'",
                        sourceModule.getId(), sourceModule.getRequiredSensorTypes(), sourceNode.getId());
                sb.append(delimiterString.apply(++step)).append(sensorString);
            }

            sb
                    .append(delimiterString.apply(++step))
                    .append(sourceModule.getProcessingTimeString(sourceNode))
                    .append(delimiterString.apply(++step))
                    .append(message.createMessageTransferTimeString(sourceNode, destinationNode));
            if (++iteration == application.getMessages().size()) {
                // last message -> also print destination
                sb.append(delimiterString.apply(++step))
                        .append(destinationModule.getProcessingTimeString(destinationNode));
            }
        }
        sb
                .append("\n")
                .append("-------------------------\n")
                .append(String.format("--> Total time: %sms\n", Utils.round(this.getTotalLatency())))
                .append("-------------------------\n");
        return sb.toString();
         */
        return null;
    }

    private String createFogNodeUsageString() {
        StringBuilder sb = new StringBuilder()
                .append("Fog node details\n")
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
