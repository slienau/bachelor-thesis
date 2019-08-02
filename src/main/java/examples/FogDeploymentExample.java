package examples;

import algorithm.deployment.AppDeployment;
import algorithm.deployment.Search;
import algorithm.application.Application;
import algorithm.infrastructure.SensorType;
import algorithm.infrastructure.Infrastructure;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FogDeploymentExample {
    public static void main(String[] args) {
        Infrastructure infrastructure = new Infrastructure();

        // create fog nodes
        infrastructure.createFogNode("raspi-01", 1024, 32, 4, 1000);
        infrastructure.createFogNode("raspi-02", 1024 * 4, 32, 4, 3000);
        infrastructure.createFogNode("mbp", 1024 * 16, 512, 8, 20000);
//        infrastructure.createFogNode("mbp", 1024 * 16, 512, 8, 20000); // error: already exists
//        infrastructure.removeFogNode("mbp");
//        infrastructure.removeFogNode("mbp"); // error (log only): remove twice

        // create sensors
        infrastructure.getFogNodeById("raspi-01").addSensor("camera-01", SensorType.CAMERA);

        // create uplinks
        infrastructure.createUplinks("raspi-01", "raspi-02", 1, 1000.0, 1000.0);
        infrastructure.createUplinks("raspi-01", "mbp", 15, 10, 250);
        infrastructure.createUplinks("raspi-02", "mbp", 15, 250, 250);
//        infrastructure.createUplinks("raspi-02", "mbp", 10, 10, 10); // error: create existing uplink

        // create application and application modules
        Application objectDetectionApp = new Application("object-detection", 100);
        objectDetectionApp.addModule("camera-controller", 100, 0.2, Arrays.asList(SensorType.CAMERA));
        objectDetectionApp.addModule("object-detector", 2000, 1.5);
        objectDetectionApp.addModule("image-viewer", 300, 0.1);

        // add
        objectDetectionApp.addMessage("IMAGE_ORIGINAL", "camera-controller", "object-detector", 500);
        objectDetectionApp.addMessage("IMAGE_DETECTED", "object-detector", "image-viewer", 500);
//        objectDetectionApp.addMessage("IMAGE_DETECTED", "adfg", "image-viewer", 500); // error: module not found


        Search s = new Search(objectDetectionApp, infrastructure);

        List<AppDeployment> validDeployments = s.getValidAppDeployments();

//        validDeployments.forEach(AppDeployment::printUsage);
//        validDeployments.get(0).getTotalLatency();
//        validDeployments.forEach(AppDeployment::calculateTotalTransferTime);

    }

}
