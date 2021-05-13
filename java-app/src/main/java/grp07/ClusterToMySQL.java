package grp07;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import common.ClientToClient;
import org.bson.types.ObjectId;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ClusterToMySQL implements ClientToClient<Measurement> {
    private static final String SOURCE_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String SOURCE_DB = "g07";
    private static final String TARGET_URL_CLOUD = "jdbc:mysql://194.210.86.10:3306/aluno_g07_cloud";
    private static final String TARGET_URL_LOCAL = "jdbc:mysql://localhost:3306/g07_local";
    private static final int CADENCE_SECONDS = 5;

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public ClusterToMySQL(String[] collectionNames) {
        this.buffer = new ConcurrentHashMap<>();
        for (String collection : collectionNames)
            this.buffer.put(collection, new LinkedBlockingQueue<>());
    }

    public static void main(String[] args) {


        String[] collectionNames = {
                "sensort1",
                "sensort2",
        };
        ClusterToMySQL ctm = new ClusterToMySQL(collectionNames);
        ctm.startFetching();
        ctm.startPublishing();
    }

    @Override
    public void startFetching() {

        MeasurementMongoFetcher fetcher = new MeasurementMongoFetcher(SOURCE_URI, SOURCE_DB);
        fetcher.deal(getBuffer());

    }

    @Override
    public void startPublishing() {
        try {

            Connection mysqlConn = DriverManager.getConnection(TARGET_URL_LOCAL, "root", "");
            new MongoToMySql(mysqlConn, MySqlData.get(), CADENCE_SECONDS).serveSQL(this.buffer);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> getBuffer() {
        return this.buffer;
    }

    private static class MeasurementMongoFetcher extends MongoHandler<Measurement> {
        private static final long SLEEP_TIME = 5000;

        public MeasurementMongoFetcher(String uri, String db) {
            super(uri, db);
        }

        @Override
        protected void deal(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collectionName, buffer) ->
                    new Thread(() -> {

                        MongoCollection<Measurement> collection = getCollection(collectionName, Measurement.class);

                        Measurement doc = getLastObject(collection);

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
                                    System.out.println("Fetched: " + doc.getId());
                                }
                                Thread.sleep(SLEEP_TIME);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start()
            );
        }
    }
}
