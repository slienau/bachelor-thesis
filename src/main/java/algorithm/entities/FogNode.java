package algorithm.entities;

import algorithm.application.Application;
import algorithm.application.ApplicationModule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class FogNode {
    private final String id;
    private final int ramTotal;
    private final int storageTotal;
    private final int cpuCores;
    private final int cpuScoreSingleCore;
    private List<ApplicationModule> deployedModules;

    public FogNode(String id, int ramTotal, int storageTotal, int cpuCores, int cpuScoreSingleCore) {
        this.id = id;
        this.ramTotal = ramTotal;
        this.storageTotal = storageTotal;
        this.cpuCores = cpuCores;
        this.cpuScoreSingleCore = cpuScoreSingleCore;
        this.deployedModules = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public int getRamTotal() {
        return ramTotal;
    }

    public int getStorageTotal() {
        return storageTotal;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public int getCpuScoreSingleCore() {
        return cpuScoreSingleCore;
    }

    public boolean deployModule(ApplicationModule appModule) {
        System.out.println(String.format("Trying to deploy %s on %s", appModule.getId(), this.getId()));
        if (appModule.getRequiredRam() > this.getRamFree() || appModule.getRequiredStorage() > this.getStorageFree()) {
            System.out.println(String.format("%s can not be deployed to %s", appModule.getId(), this.getId()));
            return false;
        }

        deployedModules.add(appModule);
        System.out.println(String.format("%s successfully deployed to %s; freeRam: %s; freeStorage: %s", appModule.getId(), this.getId(), this.getRamFree(), this.getStorageFree()));
        return true;
    }

    public boolean undeployModule(ApplicationModule moduleToUndeploy) {
        int sizeBefore = this.deployedModules.size();
        Iterator<ApplicationModule> itr = this.deployedModules.iterator();
        while (itr.hasNext()) {
            ApplicationModule module = itr.next();
            if (module.getId().equals(moduleToUndeploy.getId()))
                itr.remove();
        }
        if (sizeBefore == this.deployedModules.size())
            return false;
        else
            return true;
    }

    public int getRamFree() {
        int ramFree = this.ramTotal;
        for (ApplicationModule module : this.deployedModules) {
            ramFree -= module.getRequiredRam();
        }
        return ramFree;
    }

    public int getStorageFree() {
        int storageFree = this.storageTotal;
        for (ApplicationModule module : this.deployedModules) {
            storageFree -= module.getRequiredStorage();
        }
        return storageFree;
    }

    @Override
    public String toString() {
        return "FogNode{" +
                "id='" + id + '\'' +
                ", ram=" + ramTotal +
                ", storage=" + storageTotal +
                ", cpuCores=" + cpuCores +
                ", cpuScoreSingleCore=" + cpuScoreSingleCore +
                '}';
    }
}
