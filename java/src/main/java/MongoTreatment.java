import com.mongodb.client.MongoCollection;
import org.bson.Document;

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
            float media = 0;
            while (buffer.peek() != null){
                MeasurementPOJO meas = new MeasurementPOJO();
                Document d = buffer.poll();


            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
