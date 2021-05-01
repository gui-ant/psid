package grp02;

import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.LinkedBlockingQueue;

public class BrokerSubscriber extends BrokerConnector {

    LinkedBlockingQueue<Measurement> buffer;

    public BrokerSubscriber(String URI, String topic, int qos) throws MqttException {
        super(URI, topic, qos);
        buffer = new LinkedBlockingQueue<Measurement>();

        client.setCallback(inserInBufferCallback());
        tryConnect();
        client.subscribe(topic, this.qos);
    }

    public LinkedBlockingQueue<Measurement> getBuffer() { return buffer; }

    private MqttCallback inserInBufferCallback(){
        return new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) { System.err.println("Connection lost!"); }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                String[] message_info = MyUtils.messageIntoArray(message);
                for (String s: message_info) { System.out.print(s); }

                try {
                    Measurement measurement = MyUtils.buildMeasurement(message_info);
                    buffer.offer(measurement);

                } catch (IllegalArgumentException e) {
                    System.err.println("Illegal argument: Array size was incorrect");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        };
    }
}
