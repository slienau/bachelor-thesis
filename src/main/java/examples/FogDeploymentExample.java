package examples;

import algorithm.application.AppModule;
import algorithm.application.AppModuleConnection;
import algorithm.deployment.AppDeployment;
import algorithm.deployment.Search;
import algorithm.application.Application;
import algorithm.infrastructure.SensorType;
import algorithm.infrastructure.Infrastructure;

import java.util.List;
import java.util.stream.Collectors;

public class FogDeploymentExample {
    public static void main(String[] args) {
        Infrastructure infrastructure = new Infrastructure();

        // create fog nodes
        infrastructure.createFogNode("raspi-01", 1024, 32, 4, 1000);
        infrastructure.createFogNode("raspi-02", 1024 * 4, 32, 4, 3000);
        infrastructure.createFogNode("mbp", 1024 * 16, 512, 8, 20000);
//        infrastructure.createFogNode("mbp", 1024 * 16, 512, 8, 20000);
//        infrastructure.removeFogNode("mbp");
//        infrastructure.removeFogNode("mbp");

        // create sensors
        infrastructure.getFogNodeById("raspi-01").addSensor("camera-01", SensorType.CAMERA);

        // create uplinks
        infrastructure.createUplinks("raspi-01", "raspi-02", 1, 1000.0, 1000.0);
        infrastructure.createUplinks("raspi-01", "mbp", 15, 250, 250);
        infrastructure.createUplinks("raspi-02", "mbp", 15, 250, 250);

        AppModule cameraController = new AppModule("camera-controller", 100, 0.2);
        cameraController.addRequiredSensorType(SensorType.CAMERA);
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

}
