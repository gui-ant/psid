package grp07;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import common.IniConfig;
import common.MigrationMethod;
import org.bson.types.ObjectId;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ClusterToMySQL extends IniConfig {

    HashMap<String, LinkedBlockingQueue<Measurement>> buffer = new HashMap<>();

    public ClusterToMySQL(String iniFile) {
        super(iniFile);

        String mongoLocalUri = getConfig("mongo", "local_uri");
        String mongoLocalDb = getConfig("mongo", "local_db");
        String[] collectionNames = getConfig("mongo", "collections").split(",");

        String mysqlLocalUri = getConfig("mysql", "local_uri");

        long sleepTime = Long.valueOf(getConfig("cluster_to_mysql", "sleep_time"));
        MigrationMethod method = MigrationMethod.getByValue(getConfig("cluster_to_mysql", "method"));

        for (String collection : collectionNames)
            buffer.put(collection, new LinkedBlockingQueue<>());

        System.out.println(method);
        switch (method) {
            case DIRECT:
                MeasurementMongoFetcher m = new MeasurementMongoFetcher(mongoLocalUri, mongoLocalDb, sleepTime);
                m.deal(buffer);
                try {

                    Connection mysqlConn = DriverManager.getConnection(mysqlLocalUri, "root", "");
                    MongoToMySql mtm = new MongoToMySql(mysqlConn, new MySqlData(iniFile), sleepTime);
                    mtm.serveSQL(this.buffer);

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                break;
            case MQTT:
                new grp02.MongoToBroker(iniFile);
                new grp02.ConnectionSQL(iniFile);
                break;
        }
    }

    public static void main(String[] args) {
        new ClusterToMySQL("config.ini");
    }

    private static class MeasurementMongoFetcher extends MongoHandler<Measurement> {

        private final long sleepTime;

        public MeasurementMongoFetcher(String uri, String db, long sleepTime) {
            super(uri, db);
            this.sleepTime = sleepTime;
        }

        @Override
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collectionName, buffer) -> {

                        new Thread(() -> {
                            MongoCollection<Measurement> collection = getCollection(collectionName, Measurement.class);

                            Measurement doc = getLastObject(collection); // ultimo da colletion
                            // TODO: Considerar a collection estar vazia,i.e. gerar doc = null
                            ObjectId lastId = doc.getId();
                            while (true) {

                                try {
                                    System.out.println("Fetching " + getCollectionName(collection) + "...");

                                    MongoCursor<Measurement> cursor = collection.find(Filters.gt("_id", lastId)).iterator();

                                    // Le os novos dados e adiciona-os ao buffer
                                    while (cursor.hasNext()) {
                                        doc = cursor.next();
                                        lastId = doc.getId();
                                        buffer.offer(doc);
                                        System.out.println("Fetched:\t" + doc);
                                    }
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
            );
        }
    }
}
