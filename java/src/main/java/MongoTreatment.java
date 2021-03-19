import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.concurrent.LinkedBlockingQueue;

public class MongoTreatment extends ConnectToMongo.DocumentPublisher {

    private final LinkedBlockingQueue<Document> buffer;

    MongoTreatment(MongoCollection<Document> collection, LinkedBlockingQueue<Document> buffer) {
        super(collection, buffer);
        this.buffer = buffer;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(5000);
            float media, acc = 0;
            int counter = 0;
            while (buffer.peek() != null){

                Document doc = buffer.poll();

                //accumula o valor da medição lida
                acc += doc.get("Medicao", Float.class);

                //conta a acumulação para depois dividir no fim
                counter++;
            }
            media = acc/counter;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Extrai os campos do doc e instancía uma measurement com os mesmos
    private static MeasurementPOJO convertDocToMeasurement(Document document){
        ObjectId id = document.getObjectId("_id");
        String zone = document.getString("Zona");
        String sensor = document.getString("Sensor");
        String date = document.getString("Data");
        String measurement = document.getString("Medicao");

        return new MeasurementPOJO(id, zone, sensor, date, measurement);
    }

}
