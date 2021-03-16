import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.sql.Connection;
import java.sql.DriverManager;

public class ClusterToMySQL {
    private static final String SOURCE_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String TARGET_URL = "jdbc:mysql://194.210.86.10:3306/aluno_g07";


    public static void main(String[] args) {
        try {
            Connection sqlConn = DriverManager.getConnection(TARGET_URL, "aluno", "aluno");
            MongoDatabase sourceDB = MongoClients.create(SOURCE_URI).getDatabase("g07");

            SqlSender sender = new SqlSender(sqlConn);

            //MongoToSql st1 = new MongoToSql(sourceDB, "sensort1", sqlConn, sender);
            MongoToSqlPOJO st1 = new MongoToSqlPOJO(sourceDB, "sensort1", sqlConn, sender);
            st1.start();
        } catch (Exception e) {
            System.err.println("BRUH!");
        }


    }
}
