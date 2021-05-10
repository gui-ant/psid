package common;

import grp07.SqlDataHandler;

import java.sql.*;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class SqlPublisher<T> extends Thread {
    private SqlConnector connector;
    private LinkedBlockingQueue<T> buffer;

    public SqlPublisher(SqlConnector connector, LinkedBlockingQueue<T> buffer) throws SQLException {
        this.connector = connector;
        this.buffer = buffer;
    }

    protected abstract PreparedStatement getStatement(T object) throws SQLException;

    @Override
    public void run() {
        PreparedStatement stmt = null;
        T obj = null;
        while (true) {
            try {
                obj = buffer.take();
                System.out.println("To insert: " + obj);
                try {
                    stmt = getStatement(obj);
                    try {
                        int rows = stmt.executeUpdate();
                        if (rows > 0) {
                            System.out.println("Inserted value successfully!!!");
                        }
                        stmt.close();
                    } catch (Exception e) {
                        System.out.println("Connection failed!!!");
                        e.printStackTrace();
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected Connection getConnection() {
        return this.connector.getConnection();
    }
}
