package examples;

import algorithm.application.Application;
import algorithm.deployment.AppDeployment;
import algorithm.deployment.Search;
import algorithm.infrastructure.Infrastructure;

import java.util.Arrays;

public class FogDeploymentExample {
    public static void main(String[] args) {
        Infrastructure infrastructure = new Infrastructure();

        // create fog nodes
        infrastructure.addFogNode("raspi-01", 1024, 32, 4, 1000, Arrays.asList("CAMERA"));
        infrastructure.addFogNode("raspi-02", 1024 * 4, 32, 4, 3000);
        infrastructure.addFogNode("mbp", 1024 * 16, 512, 8, 15623);
//        infrastructure.addFogNode("mbp", 1024 * 16, 512, 8, 20000); // error: already exists
//        infrastructure.removeFogNode("mbp");
//        infrastructure.removeFogNode("mbp"); // error (log only): remove twice
//        infrastructure.getFogNode("randomFogNode"); // error: node doesn't exist

        // create uplinks
        infrastructure.addNetworkLink("raspi-01", "raspi-02", 1, 1000.0, 1000.0);
        infrastructure.addNetworkLink("raspi-01", "mbp", 15, 10, 250);
        infrastructure.addNetworkLink("raspi-02", "mbp", 15, 250, 250);
//        infrastructure.addNetworkLink("raspi-02", "mbp", 10, 10, 10); // error: create existing uplink

        // create application and application modules
        Application objectDetectionApp = new Application("object-detection");
        objectDetectionApp.addHardwareModule("CAMERA");
        objectDetectionApp.addSoftwareModule("camera-controller", 100, 0.2, 100);
        objectDetectionApp.addSoftwareModule("object-detector", 2000, 1.5, 5000);
        objectDetectionApp.addSoftwareModule("image-viewer", 300, 0.1, 100);

        // add messages
        objectDetectionApp.addMessage("IMAGE_RAW", "CAMERA", "camera-controller", 1000);
        objectDetectionApp.addMessage("IMAGE_ORIGINAL", "camera-controller", "object-detector", 500);
        objectDetectionApp.addMessage("IMAGE_DETECTED", "object-detector", "image-viewer", 500);
//        objectDetectionApp.addMessage("IMAGE_DETECTED", "adfg", "image-viewer", 500); // error: module not found

        // add loop
        objectDetectionApp.addLoop("object-detection", 1000, Arrays.asList("CAMERA", "camera-controller", "object-detector", "image-viewer"));

        Search s = new Search(objectDetectionApp, infrastructure);
        AppDeployment fastestDeployment = s.getFastestDeployment();
        System.out.println(String.format("Fastest deployment is %s", fastestDeployment));
        System.out.println(fastestDeployment.createDetailsString());

//        validDeployments.forEach(AppDeployment::printUsage);
//        validDeployments.get(0).getTotalLatency();
//        validDeployments.forEach(AppDeployment::calculateTotalTransferTime);

    }

}
