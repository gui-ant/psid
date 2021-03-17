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
    private static final String TARGET_URL = "jdbc:mysql://194.210.86.10:3306/aluno_g07";


    public static void main(String[] args) {
        try {

            // create codec registry for POJOs
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            // get handle to database

            MongoClient cluster = MongoClients.create(SOURCE_URI);
            MongoDatabase database = cluster.getDatabase("g07").withCodecRegistry(pojoCodecRegistry);

            //MongoDatabase cluster = MongoClients.create(SOURCE_URI).getDatabase("g07");
            Connection mysql = DriverManager.getConnection(TARGET_URL, "aluno", "aluno");

            //MongoToSql st1 = new MongoToSql(sourceDB, "sensort1", sqlConn, sender);
            MongoToSqlPOJO st1 = new MongoToSqlPOJO(database, "sensort1", mysql);
            st1.start();
        } catch (Exception e) {
            System.err.println("BRUH!");
        }


    }
}
