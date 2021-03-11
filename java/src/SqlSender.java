import com.google.gson.Gson;
import org.bson.Document;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class SqlSender {
    private Connection connection;

    //apenas estou a considerar o registo de measures, mais tarde faz-se o refactor
//    int id          = 4;
//    String data     = "100";
//    int sensor_id   = 1;
//    int zone_id     = 1;

    public SqlSender (Connection connection) {
        this.connection = connection;
    }


    public synchronized void send (Connection connection, Measurement measurement) {
        // buscar dados e extrair valores

        System.out.println(measurement);
        try {
//            String sql = "INSERT INTO measures (id, data, sensor_id, zone_id) VALUES (?, ?, ?, ?)";
//            PreparedStatement statement = connection.prepareStatement(sql);
//            statement.setInt(1, id);
//            statement.setString(2, data);
//            statement.setInt(3, sensor_id);
//            statement.setInt(4, zone_id);
//            int rows = statement.executeUpdate();
//            System.out.println("Inserted value successfully!!!");
//            statement.close();

            //obter atributos do objeto measurement
            int zone_id = Integer.parseInt(measurement.getZona().substring(1));
            int sensor_id = Integer.parseInt(measurement.getSensor().substring(1));
            String data = measurement.getMedicao();

            //enviar para SQL
            String sql = "INSERT INTO measures (data, sensor_id, zone_id) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, data);
            statement.setInt(2, sensor_id);
            statement.setInt(3, zone_id);

            int rows = statement.executeUpdate();
            if (rows > 0){
                System.out.println("Inserted value successfully!!!");
            }
        } catch (Exception e) {
            System.out.println("Connection failed!!!");
            e.printStackTrace();
        }
    }

    /*
    public synchronized void send (Connection connection, Document sendable) {
        //enviar para SQL
        System.out.println(sendable.toJson());

    }
     */
}
