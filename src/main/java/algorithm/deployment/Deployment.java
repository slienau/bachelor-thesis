package algorithm.deployment;

import algorithm.application.ApplicationModule;
import algorithm.entities.FogNode;

import java.util.HashMap;
import java.util.Map;

public class Deployment {
    public Map<ApplicationModule, FogNode> moduleToFogMap;

    public Deployment() {
        this.moduleToFogMap = new HashMap<>();
    }
}
