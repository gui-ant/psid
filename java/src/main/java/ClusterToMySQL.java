import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class ClusterToMySQL {
    private static final String SOURCE_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String SOURCE_DB = "g07";
    private static final String TARGET_URL_CLOUD = "jdbc:mysql://194.210.86.10:3306/aluno_g07_cloud";
    private static final String TARGET_URL_LOCAL = "jdbc:mysql://194.210.86.10:3306/aluno_g07_local";
    private static final int CADENCE_SECONDS = 5;


    public static void main(String[] args) {
        try {

            String[] collectionNames = {
                    "sensort1",
                    "sensort2",
            };

            // create codec registry for POJOs
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));


            Connection mysqlCloud = DriverManager.getConnection(TARGET_URL_CLOUD, "aluno", "aluno");
            Connection mysqlLocal = DriverManager.getConnection(TARGET_URL_LOCAL, "aluno", "aluno");


            ConnectToMongo from_cluster = new ConnectToMongo(SOURCE_URI, SOURCE_DB);
            MongoToSQL to_sql = new MongoToSQL(mysqlLocal, new SqlSender(mysqlCloud), CADENCE_SECONDS);

            from_cluster.useCollections(collectionNames);

            from_cluster.startFetching();

            to_sql.serveSQL(from_cluster.getFetchingSource());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
