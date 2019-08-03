package algorithm.application;

import algorithm.deployment.AppDeployment;

import java.util.LinkedList;
import java.util.List;

public class AppLoop {
    private final String loopName;
    private final int maxLatency;
    private final LinkedList<String> modules = new LinkedList<>();

    public AppLoop(String loopName, int maxLatency, List<String> modules) {
        this.loopName = loopName;
        this.maxLatency = maxLatency;
        this.modules.addAll(modules);
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
        return this.getTransferTime(appDeployment) + this.getProcessingTime(appDeployment);
    }

    public double getTransferTime(AppDeployment appDeployment) {
        // TODO: calculate transfer time for deployment for this loop
        return 13.0;
    }

    public double getProcessingTime(AppDeployment appDeployment) {
        // TODO: calculate processing time for deployment for this loop
        return 10.0;
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
