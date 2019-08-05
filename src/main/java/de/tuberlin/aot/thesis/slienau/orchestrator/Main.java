package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.orchestrator.models.Heartbeat;

import java.util.HashMap;

public class Main {


    public static void main(String args[]) throws Exception {

        System.out.println("Starting ...\n");

        String broker = "tcp://localhost:1883";
        String topic = "/devices/#";
        HashMap<String, Heartbeat> nodes = new HashMap<>();
        Thread monitorThread = new Thread(new Monitor(broker, topic, nodes));
        monitorThread.start();

        Runnable r = () -> {
            while (!Thread.interrupted()) {
                System.out.println("Current nodes:\t" + nodes);
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {

                }
            }
        };

        new Thread(r).start();

    }

}
