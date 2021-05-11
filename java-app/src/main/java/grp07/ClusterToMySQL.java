package grp07;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class ClusterToMySQL {
    private static final String SOURCE_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String SOURCE_DB = "g07";
    private static final String TARGET_URL_CLOUD = "jdbc:mysql://194.210.86.10:3306/aluno_g07_cloud";
    private static final String TARGET_URL_LOCAL = "jdbc:mysql://localhost:3306/g07_local";
    private static final int CADENCE_SECONDS = 5;


    public static void main(String[] args) {
        try {

            String[] collectionNames = {
                    "sensort1",
                    "sensort2",
            };

            Connection mysqlCloud = DriverManager.getConnection(TARGET_URL_CLOUD, "aluno", "aluno");
            Connection mysqlLocal = DriverManager.getConnection(TARGET_URL_LOCAL, "root", "");


            ConnectToMongo sourceCluster = new ConnectToMongo(SOURCE_URI, SOURCE_DB);
            MongoToSQL targetMySql = new MongoToSQL(mysqlLocal, new SqlSender(mysqlCloud,mysqlLocal), CADENCE_SECONDS);

            sourceCluster.useCollections(collectionNames);

            sourceCluster.startFetching();

            targetMySql.serveSQL(sourceCluster.getFetchingSource());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
