package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.utils.MqttUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttCommandExecutor implements Runnable {
    private final String broker;
    private final String deviceName;
    private final String command;
    private final byte[] payload;
    private byte[] result;

    public MqttCommandExecutor(String broker, String deviceName, String command, byte[] payload) {
        this.broker = broker;
        this.deviceName = deviceName;
        this.command = command;
        if (payload != null)
            this.payload = payload;
        else
            this.payload = "".getBytes();
    }

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    @Override
    public void run() {
//        System.out.println(String.format("[MqttCommandExecutor][%s] Going to execute command '%s'", deviceName, command));
        try {
            MqttClient mqttClient = new MqttClient(broker, MqttClient.generateClientId(), new MemoryPersistence());
            mqttClient.setCallback(new CommandResultCallback(this));
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            mqttClient.connect(connOpts);
            MqttMessage message = new MqttMessage(payload);
            message.setQos(2);
            mqttClient.subscribe(String.format("/devices/%s/commands/out/%s", deviceName, command));
            mqttClient.publish(String.format("/devices/%s/commands/in/%s", deviceName, command), message);

            while (result == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mqttClient.disconnect();
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    static class CommandResultCallback implements MqttCallback {
        private final MqttCommandExecutor commandExecutor;

        public CommandResultCallback(MqttCommandExecutor commandExecutor) {
            this.commandExecutor = commandExecutor;
        }

        @Override
        public void connectionLost(Throwable cause) {
            System.err.println("[MqttCommandExecutor] Connection lost");
            cause.printStackTrace();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            commandExecutor.setResult(message.getPayload());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    }
}
