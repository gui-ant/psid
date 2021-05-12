package grp07;

import common.ClientToClient;
import common.MongoFetcher;
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

    private static class MeasurementMongoFetcher extends MongoFetcher<Measurement> {
        public MeasurementMongoFetcher(String uri, String db) {
            super(uri, db);
        }

        @Override
        protected ObjectId getObjectId(Measurement doc) {
            return doc.getId();
        }

        @Override
        protected Class<Measurement> getMapperClass() {
            return Measurement.class;
        }
    }
}
