import org.bson.Document;
import java.sql.Connection;

public class SqlSender {
    private Connection connection;

    public SqlSender (Connection connection) {
        this.connection = connection;
    }

    public synchronized void send (Connection connection, Document sendable) {
        //enviar para SQL
        System.out.println(sendable.toJson());
    }
}
