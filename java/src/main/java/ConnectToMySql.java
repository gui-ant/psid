import org.bson.Document;

import java.sql.*;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectToMySql {

    private Connection conn;

    public static void main(String[] args) throws Exception {
        ConnectToMySql test = new ConnectToMySql();
        test.getConnection();
        test.testSelectQuery();
    }

    public void getConnection() throws Exception {

        try {


            // Server sql disponibilizado pelos docentes com a db sid2021
            String url = "jdbc:mysql://194.210.86.10/sid2021";
            String username = "aluno";
            String password = "aluno";

            //Comandos especificados pela documentação -> https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-usagenotes-connect-drivermanager.html
            String driver = "com.mysql.cj.jdbc.Driver";
            Class.forName(driver).newInstance();


            this.conn = DriverManager.getConnection(url + "?" + "user=aluno&password=aluno");
            System.out.println("Connected");


        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void testSelectQuery() throws SQLException {

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM sensor");

            while (rs.next()) {
                System.out.println(rs.getString("limiteinferior"));
            }
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore
                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore
                stmt = null;
            }
        }

    }

    public void populate(ConcurrentHashMap<String, LinkedBlockingQueue<Document>> fetchingSource) {

    }
}
