package examples;

import algorithm.application.AppModule;
import algorithm.application.AppModuleConnection;
import algorithm.deployment.AppDeployment;
import algorithm.deployment.Search;
import algorithm.entities.FogNode;
import algorithm.application.Application;
import algorithm.entities.SensorType;
import algorithm.infrastructure.Infrastructure;
import algorithm.infrastructure.NetworkConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FogDeploymentExample {
    public static void main(String[] args) {
        Infrastructure infrastructure = new Infrastructure();

        FogNode raspi1 = new FogNode("raspi-01", 1024, 32, 4, 1000);
//        Sensor camera1 = new Sensor("camera-01", SensorType.CAMERA);
//        raspi1.addConnectedThing(camera1);
        infrastructure.addFogNode(raspi1);

        FogNode raspi2 = new FogNode("raspi-02", 1024 * 4, 16, 4, 3000);
        infrastructure.addFogNode(raspi2);

        FogNode mbp = new FogNode("dsl-mbp", 1024 * 16, 512, 8, 20000);
//        infrastructure.addFogNode(mbp);

        infrastructure.addNetworkConnection(new NetworkConnection("raspi-01", "raspi-02", 1, 1000.0, 1000.0));

        AppModule cameraController = new AppModule("camera-controller", 100, 0.2);
        AppModule objectDetector = new AppModule("object-detector", 2000, 1.5);
        AppModule imageViewer = new AppModule("image-viewer", 300, 0.1);

        Application objectDetectionApp = new Application("object-detection", 500);
        objectDetectionApp.addModuleConnection(new AppModuleConnection(cameraController, objectDetector, 500));
        objectDetectionApp.addModuleConnection(new AppModuleConnection(objectDetector, imageViewer, 500));


        Search s = new Search(objectDetectionApp, infrastructure);

        List<AppDeployment> validDeployments = s.getValidAppDeployments();

        System.out.println("Valid Deployments:" + validDeployments.stream().map(dep -> "\n\t" + dep.toString()).collect(Collectors.joining()));

        validDeployments.forEach(AppDeployment::printUsage);

    }

    private static List<String> asList(String... strings) {
        List<String> result = new ArrayList<>();
        Collections.addAll(result, strings);
        return result;
    }

    private static List<SensorType> asList(SensorType... thingTypes) {
        List<SensorType> result = new ArrayList<>();
        Collections.addAll(result, thingTypes);
        return result;
    }

}
