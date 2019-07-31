package examples;

import algorithm.application.ApplicationModuleConnection;
import algorithm.deployment.AppDeployment;
import algorithm.deployment.Search;
import algorithm.entities.FogNode;
import algorithm.application.Application;
import algorithm.application.ApplicationModule;
import algorithm.entities.SensorType;
import algorithm.infrastructure.Infrastructure;
import algorithm.infrastructure.NetworkConnection;

import java.util.ArrayList;
import java.util.List;

public class FogDeploymentExample {
    public static void main(String[] args) {
        Infrastructure infrastructure = new Infrastructure();

        FogNode raspi1 = new FogNode("raspi-01", 1024, 32, 4, 1000);
//        Sensor camera1 = new Sensor("camera-01", SensorType.CAMERA);
//        raspi1.addConnectedThing(camera1);
        infrastructure.addFogNode(raspi1);

        FogNode raspi2 = new FogNode("raspi-02", 4096, 16, 4, 3000);
        infrastructure.addFogNode(raspi2);

        FogNode raspi3 = new FogNode("raspi-03", 4096 * 4, 512, 8, 20000);
        infrastructure.addFogNode(raspi3);

        infrastructure.addNetworkConnection(new NetworkConnection("raspi-01", "raspi-02", 1, 1000.0, 1000.0));

        ApplicationModule cameraController = new ApplicationModule("camera-controller", 50, 0.2);
        ApplicationModule objectDetector = new ApplicationModule("object-detector", 2048, 1.5);
        ApplicationModule imageViewer = new ApplicationModule("image-viewer", 256, 0.1);

        Application objectDetectionApp = new Application("object-detection", 500);
        objectDetectionApp.addModuleConnection(new ApplicationModuleConnection(cameraController, objectDetector, 500));
        objectDetectionApp.addModuleConnection(new ApplicationModuleConnection(objectDetector, imageViewer, 500));


        Search s = new Search(objectDetectionApp, infrastructure);
//        s.printInfo();

        List<AppDeployment> uncheckedAppDeployments = s.getAppDeploymentsUnchecked();
        for (AppDeployment dep : uncheckedAppDeployments) {
            System.out.println(dep);
        }

    }

    private static List<String> asList(String... strings) {
        List<String> result = new ArrayList<>();
        for (String s : strings) {
            result.add(s);
        }
        return result;
    }

    private static List<SensorType> asList(SensorType... thingTypes) {
        List<SensorType> result = new ArrayList<>();
        for (SensorType tt : thingTypes) {
            result.add(tt);
        }
        return result;
    }

}
