package grp07;

import common.SqlConnector;

public class MongoToMySQL {
    private static final String SOURCE_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String SOURCE_DB = "g07";
    private static final String TARGET_URL_CLOUD = "jdbc:mysql://194.210.86.10:3306/aluno_g07_cloud";
    private static final String TARGET_URL_LOCAL = "jdbc:mysql://localhost/g07_local";
    private static final int CADENCE_SECONDS = 5;


    public static void main(String[] args) {
        try {

            String[] collectionNames = {
                    "sensort1",
                    "sensort2",
            };

            SqlConnector mysqlCloud = new SqlConnector(TARGET_URL_CLOUD, "aluno", "aluno");
            SqlConnector mysqlLocal = new SqlConnector(TARGET_URL_LOCAL, "root", "");

            MongoMeasurementsHandler sourceCluster = new MongoMeasurementsHandler(SOURCE_URI, SOURCE_DB);
            MongoToSQL targetMySql = new MongoToSQL(mysqlLocal, new SqlDataHandler(), CADENCE_SECONDS);

            sourceCluster.useCollections(collectionNames);
            sourceCluster.startFetching();

            targetMySql.serveSQL(sourceCluster.getCollectionsBuffer());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
