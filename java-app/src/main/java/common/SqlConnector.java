package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlConnector {
    private final Connection connection;
    private String database;

    public SqlConnector(String sourceUri, String user, String pass) throws SQLException {
        this.connection = DriverManager.getConnection(sourceUri, user, pass);
    }

    public void useDatabase(String db) {
        this.database = db;
    }

    public Connection getConnection() {
        return connection;
    }
}
