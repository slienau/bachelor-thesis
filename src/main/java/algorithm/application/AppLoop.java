package algorithm.application;

import algorithm.deployment.AppDeployment;
import algorithm.infrastructure.FogNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AppLoop {
    private final String loopName;
    private final int maxLatency;
    private final LinkedList<String> modules = new LinkedList<>();

    public AppLoop(String loopName, int maxLatency, List<String> modules) {
        this.loopName = loopName;
        this.maxLatency = maxLatency;
        this.modules.addAll(modules);
    }

    private static boolean inputEqualsOutputType(AppModule sourceModule, AppModule destinationModule) {
        return sourceModule.getOutputType().equals(destinationModule.getInputType());
    }

    public String getLoopName() {
        return loopName;
    }

    public int getMaxLatency() {
        return maxLatency;
    }

    public LinkedList<String> getModules() {
        return modules;
    }

    public boolean hasValidLatencyRequirements(AppDeployment appDeployment, Application application) {
        return this.getTotalLatency(appDeployment, application) < this.getMaxLatency();
    }

    public double getTotalLatency(AppDeployment appDeployment, Application application) {
        return this.getTotalTransferTime(appDeployment, application) + this.getTotalProcessingTime(appDeployment, application);
    }

    public double getTotalTransferTime(AppDeployment appDeployment, Application application) {
        double transferTime = 0;
        for (int i = 0; i < this.modules.size(); i++) {
            AppModule thisModule = application.getModuleById(this.modules.get(i));
            AppModule nextModule = null;
            if (i < this.modules.size() - 1)
                nextModule = application.getModuleById(this.modules.get(i + 1));

            if (nextModule == null) {
                // thisModule is the final one --> loop end
                return transferTime;
            }

            if (!inputEqualsOutputType(thisModule, nextModule))
                throw new IllegalStateException(String.format("[AppLoop][%s] Output type of module '%s' (type '%s') not equal to input type of module '%s' (type '%s')", this.getLoopName(), thisModule.getId(), thisModule.getOutputType(), nextModule.getId(), nextModule.getInputType()));

            AppMessage message = application.getMessage(thisModule.getOutputType());
            if (message == null)
                throw new IllegalStateException(String.format("[AppLoop][%s] Unable to get message type '%s'", this.getLoopName(), thisModule.getOutputType()));

            if (thisModule instanceof AppSoftwareModule && nextModule instanceof AppSoftwareModule) {
                FogNode sourceNode = appDeployment.getNodeForSoftwareModule((AppSoftwareModule) thisModule);
                FogNode destinationNode = appDeployment.getNodeForSoftwareModule((AppSoftwareModule) nextModule);
                transferTime += message.calculateMessageTransferTime(sourceNode, destinationNode);
            }
        }
        return transferTime;
    }

    public double getTotalProcessingTime(AppDeployment appDeployment, Application application) {
        return this.getSoftwareModules(application).stream()
                .mapToDouble(softwareModule -> appDeployment.getNodeForSoftwareModule(softwareModule).calculateProcessingTimeForModule(softwareModule))
                .sum();
    }

    private List<AppSoftwareModule> getSoftwareModules(Application application) {
        return this.modules.stream()
                .map(application::getModuleById)
                .filter(module -> module instanceof AppSoftwareModule)
                .map(module -> (AppSoftwareModule) module)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public String toString() {
        return "AppLoop{" +
                "loopName='" + loopName + '\'' +
                "maxLatency='" + maxLatency + '\'' +
                ", modules=" + modules +
                '}';
    }
}
