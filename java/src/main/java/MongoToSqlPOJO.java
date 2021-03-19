import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Connection;


public class MongoToSqlPOJO extends Thread {

    private final MongoCollection<MeasurementPOJO> srcMongoCollection;
    private final Connection sqlCloudConn;
    private final Connection sqlLocalConn;
    private final SqlSender sender;

    private MeasurementPOJO measurement;
    private MeasurementPOJO lastSentMea;

    public MongoToSqlPOJO(MongoDatabase srcMongoDB, String srcMongoCollectionName, Connection sqlCloudConn, Connection sqlLocalConn) {

        this.sqlCloudConn = sqlCloudConn;
        this.sqlLocalConn = sqlLocalConn;
        this.sender = new SqlSender(sqlCloudConn);
        this.srcMongoCollection = srcMongoDB.getCollection(srcMongoCollectionName, MeasurementPOJO.class);
    }


    private void fetchData() {
        //vai buscar o ultimo doc ao Mongo
        this.measurement = getLastObjectMongo();

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
            sender.send(sqlLocalConn, measurement);
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

            fetchData();
            sendToSql();
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        }
    }

}
