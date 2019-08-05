package de.tuberlin.aot.thesis.slienau.algorithm.application;

import de.tuberlin.aot.thesis.slienau.algorithm.deployment.AppDeployment;
import de.tuberlin.aot.thesis.slienau.algorithm.infrastructure.FogNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class AppLoop {
    private final String loopName;
    private final int maxLatency;
    private final LinkedList<String> modules = new LinkedList<>();
    private final Application application;

    public AppLoop(String loopName, int maxLatency, List<String> modules, Application application) {
        this.loopName = loopName;
        this.maxLatency = maxLatency;
        this.modules.addAll(modules);
        this.application = application;
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

    public boolean hasValidLatencyRequirements(AppDeployment appDeployment) {
        return this.getTotalLatency(appDeployment) < this.getMaxLatency();
    }

    public double getTotalLatency(AppDeployment appDeployment) {
        return this.getTotalTransferTime(appDeployment) + this.getTotalProcessingTime(appDeployment);
    }

    public double getTotalTransferTime(AppDeployment appDeployment) {
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

            if (thisModule instanceof AppSoftwareModule && nextModule instanceof AppSoftwareModule) {
                FogNode sourceNode = appDeployment.getNodeForSoftwareModule((AppSoftwareModule) thisModule);
                FogNode destinationNode = appDeployment.getNodeForSoftwareModule((AppSoftwareModule) nextModule);
                transferTime += this.getAppMessageByMessageType(thisModule.getOutputType()).calculateMessageTransferTime(sourceNode, destinationNode);
            }
        }
        return transferTime;
    }

    private AppMessage getAppMessageByMessageType(String messageType) {
        AppMessage message = application.getMessage(messageType);
        if (message == null)
            throw new IllegalStateException(String.format("[AppLoop][%s] Unable to get message type '%s'", this.getLoopName(), messageType));
        return message;
    }

    public double getTotalProcessingTime(AppDeployment appDeployment) {
        return this.getSoftwareModules().stream()
                .mapToDouble(softwareModule -> appDeployment.getNodeForSoftwareModule(softwareModule).calculateProcessingTimeForModule(softwareModule))
                .sum();
    }

    private List<AppSoftwareModule> getSoftwareModules() {
        return this.modules.stream()
                .map(application::getModuleById)
                .filter(module -> module instanceof AppSoftwareModule)
                .map(module -> (AppSoftwareModule) module)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns the destination fog node (destination for the output AppMessage) for a given source module (deployed on another/same fog node)
     *
     * @param sourceModuleId
     * @return
     */
    public String getDestinationNodeForSourceModule(String sourceModuleId, AppDeployment appDeployment) {
        if (sourceModuleId == null)
            return null;
        ListIterator<String> itr = modules.listIterator(0);
        while (itr.hasNext()) {
            String module = itr.next();
            String nextModule = null;
            if (!itr.hasNext())
                break;
            if (module.equals(sourceModuleId))
                nextModule = itr.next();
            if (nextModule == null)
                continue;
            FogNode nextNode = appDeployment.getNodeForSoftwareModule(nextModule);
            return nextNode.getId();
        }
        return null;
    }

    public String getDetailString(AppDeployment appDeployment) {
        String prefix = String.format("[AppLoop][%s]", this.getLoopName());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.modules.size(); i++) {
            AppModule thisModule = application.getModuleById(this.modules.get(i));
            AppModule nextModule = null;
            if (i < this.modules.size() - 1)
                nextModule = application.getModuleById(this.modules.get(i + 1));


            if (thisModule instanceof AppHardwareModule)
                sb.append(prefix).append(String.format("[%9s] HardwareModule '%s' (connected to %s) produces message type '%s'", "0ms", thisModule.getId(), appDeployment.getNodeForSoftwareModule((AppSoftwareModule) nextModule).getId(), thisModule.getOutputType())).append("\n");
            if (thisModule instanceof AppSoftwareModule) {
                AppSoftwareModule thisModuleSw = (AppSoftwareModule) thisModule;
                FogNode processingNode = appDeployment.getNodeForSoftwareModule(thisModuleSw);

                sb.append(prefix).append(String.format("[%7sms] SoftwareModule '%s' processes incoming message '%s' on node '%s' and outputs message '%s'",
                        processingNode.calculateProcessingTimeForModule(thisModuleSw), thisModule.getId(), thisModule.getInputType(), processingNode.getId(), thisModule.getOutputType())).append("\n");

                if (nextModule == null) {
                    // thisModule is final module --> end of AppLoop
                    break;
                }

                AppMessage message = this.getAppMessageByMessageType(thisModule.getOutputType());
                FogNode sourceNode = appDeployment.getNodeForSoftwareModule((AppSoftwareModule) thisModule);
                FogNode destinationNode = appDeployment.getNodeForSoftwareModule((AppSoftwareModule) nextModule);
                double transferTime = this.getAppMessageByMessageType(thisModule.getOutputType()).calculateMessageTransferTime(sourceNode, destinationNode);
                sb.append(prefix).append(String.format("[%7sms] Transfer message with content type '%s' from '%s' to '%s'", transferTime, message.getContentType(), sourceNode.getId(), destinationNode.getId())).append("\n");
            }
        }
        sb.append(prefix).append(String.format("[%7sms] <-- TOTAL LATENCY", this.getTotalLatency(appDeployment)))
                .append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "AppLoop{" +
                "loopName='" + loopName + '\'' +
                ", maxLatency=" + maxLatency +
                ", modules=" + modules +
                ", application=" + application.getName() +
                '}';
    }
}
