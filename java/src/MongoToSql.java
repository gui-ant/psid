import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Connection;


public class MongoToSql extends Thread {

    private final MongoCollection<Document> srcMongoCollection;
    private final Connection sqlConn;
    private final SqlSender sender;
    private Measurement measurement;
    private Measurement lastSentMea;


    public MongoToSql(MongoDatabase srcMongoDB, String srcMongoCollectionName, Connection sqlConn, SqlSender sender) {
        this.sender = sender;
        this.srcMongoCollection = srcMongoDB.getCollection(srcMongoCollectionName);
        this.sqlConn = sqlConn;
    }

    private void fetchData() {
        //vai buscar o ultimo doc ao Mongo e passa de JSON para um objeto Measurement
        measurement = new Gson().fromJson(getLastObjectMongo().toJson(), Measurement.class);
        String original_mongo_id = getLastObjectMongo().get("_id").toString();
        measurement.set_id(original_mongo_id);

        //só para testes (APAGAR)
        System.out.println("---------------------------------------------------------------");
        if (lastSentMea == null)
            System.out.println("Last measure ID: " + null);
        else
            System.out.println("Last measure ID: " + lastSentMea.get_id());

        System.out.println("Mongo original ID: " + getLastObjectMongo().get("_id").toString());
        System.out.println("ID in Measure to send: " + measurement.get_id());
        System.out.println("---------------------------------------------------------------");
        //só para testes (APAGAR)
    }

    private Document getLastObjectMongo() {
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
        return !measurement.measequals(lastSentMea);
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
