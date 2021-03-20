import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.concurrent.LinkedBlockingQueue;
/*
public class MongoTreatment{

    //Extrai os campos do doc e instancía uma measurement com os mesmos
    public static MeasurementPOJO convertDocToMeasurement(Document document){
        ObjectId id = document.getObjectId("_id");
        String zone = document.getString("Zona");
        String sensor = document.getString("Sensor");
        String date = document.getString("Data");
        String measurement = document.getString("Medicao");

        return new MeasurementPOJO(id, zone, sensor, date, measurement);

    }

}
*/