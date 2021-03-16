import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.sql.Connection;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public class MongoToSqlPOJO extends Thread {

    //private final MongoCollection<Document> srcMongoCollection;
    private final MongoCollection<MeasurementPOJO> srcMongoCollection;
    private final Connection sqlConn;
    private final SqlSender sender;

    private MeasurementPOJO measurement;
    private MeasurementPOJO lastSentMea;


    public MongoToSqlPOJO(MongoDatabase srcMongoDB, String srcMongoCollectionName, Connection sqlConn, SqlSender sender) {
        // create codec registry for POJOs
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        // get handle to "mydb" database
        MongoDatabase database = srcMongoDB.withCodecRegistry(pojoCodecRegistry);

        this.sender = sender;
        //this.srcMongoCollection = srcMongoDB.getCollection(srcMongoCollectionName);
        this.srcMongoCollection = database.getCollection(srcMongoCollectionName, MeasurementPOJO.class);
        this.sqlConn = sqlConn;


    }

    private void fetchData() {
        //vai buscar o ultimo doc ao Mongo e passa de JSON para um objeto Measurement
        MeasurementPOJO doc = getLastObjectMongo();

        //measurement = new Gson().fromJson(doc.toJson(), MeasurementPOJO.class);
        measurement = doc;

        //String original_mongo_id = doc.get("_id").toString();
        ObjectId original_mongo_id = doc.getId();
        measurement.setId(original_mongo_id);

        //só para testes (APAGAR)
        System.out.println("---------------------------------------------------------------");
        if (lastSentMea == null)
            System.out.println("Last measure ID: " + null);
        else
            System.out.println("Last measure ID: " + lastSentMea.getId());

        System.out.println("Mongo original ID: " + getLastObjectMongo().getId());
        System.out.println("ID in Measure to send: " + measurement.getId());
        System.out.println("---------------------------------------------------------------");
        //só para testes (APAGAR)
    }

    private MeasurementPOJO getLastObjectMongo() {
        return srcMongoCollection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    private void sendToSql() {
        if (canSend()) {
            sender.send(sqlConn, measurement);
            lastSentMea = measurement;
        } else
            System.out.println("Repeated measure, not sent;");
    }

    private boolean canSend() {
        return !measurement.equals(lastSentMea);
    }

    public void run() {
//      while (true) {

        //só para testar se não manda a 2ª vez (APAGAR)
        for (int i = 0; i != 2; i++) {

            try {
                fetchData();
            } catch (Exception e) {
                System.err.println("Erro em fetchData()");
            }
            try {
                sendToSql();
            } catch (Exception e) {
                System.err.println("Erro em sendToSql()");
            }
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        }
    }

}
