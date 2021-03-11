import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

public class MainMongoToSQL {
    private static final String SOURCE_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String TARGET_URL = "jdbc:mysql://194.210.86.10:3306/aluno_g07";


    public static void main (String[] args) {
        try {
            Connection sqlConn = DriverManager.getConnection(TARGET_URL, "aluno", "aluno");
            //MongoClientURI sourceURI = new MongoClientURI(SOURCE_URI);
            //MongoDatabase sourceDB = new MongoClient(sourceURI).getDatabase("g07");

            SqlSender sender = new SqlSender(sqlConn);
            //teste ainda sem valores do mongo
            sender.send(sqlConn,new LinkedList<>());

            //MongoToSql st1 = new MongoToSql(sourceDB, "sensort1", sqlConn, sender);
           // st1.run();
        }
        catch (Exception e) {
            System.err.println("BRUH!");
        }


    }
}
