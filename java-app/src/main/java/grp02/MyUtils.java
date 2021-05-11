package grp02;

import grp07.Measurement;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public final class MyUtils {

    //TODO: Melhorar o martelanso, acode-me Rui!
    public static String[] messageIntoArray(MqttMessage message) {

        String[] measurement_info = new String[5];
        String[] temp = message.toString().split(",");

        for(int i = 0; i < temp.length; i++){
            measurement_info[i] = temp[i]
                    .split("=")[1]
                    .replace("'", "")
                    .replace("}", "");
        }

        return measurement_info;
    }

    public static Measurement buildMeasurement(String info[]) {

        if (info.length != 5) throw new IllegalArgumentException();

        Measurement measurement = new Measurement();
        ObjectId obj_id = new ObjectId(info[0]);

        measurement.setId(obj_id);
        measurement.setZone(info[1]);
        measurement.setSensor(info[2]);
        measurement.setDate(info[3]);
        measurement.setValue(info[4]);

        return measurement;
    }

}
