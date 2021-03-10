import org.bson.Document;

import java.sql.Connection;
import java.util.List;

public class SqlSender {
    private Connection connection;

    public SqlSender (Connection connection) {
        this.connection = connection;
    }

    public synchronized void send (Connection connection, List<Document> list) {
        //enviar para SQL
    }
}
