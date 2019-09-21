package de.tuberlin.aot.thesis.slienau.examples;

import de.tuberlin.aot.thesis.slienau.utils.MqttUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSubscribeExample {

    public static void main(String[] args) {

        class SimpleMqttCallback implements MqttCallback {

            public void connectionLost(Throwable throwable) {
                System.out.println("Connection to MQTT broker lost!");
            }

            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                System.out.println("Message received:\n\t" + new String(mqttMessage.getPayload()));
            }

            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                // not used in this example
            }
        }

        int qos = 2;
        String broker = "tcp://localhost:1883";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient client = new MqttClient(broker, MqttClient.generateClientId(), persistence);
            client.setCallback(new SimpleMqttCallback());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);
            System.out.println("Connected");
            client.subscribe("#");
            System.out.println("Subscribed");
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }
}
