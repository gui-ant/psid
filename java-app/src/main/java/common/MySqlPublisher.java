package common;

import grp07.MySqlData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MySqlPublisher<T> extends Thread {


    private final Connection connection;
    private final MySqlData data;
    private final LinkedBlockingQueue<T> buffer;

    public MySqlPublisher(Connection connection, MySqlData data, LinkedBlockingQueue<T> buffer) {
        this.connection = connection;
        this.data = data;
        this.buffer = buffer;
    }

    public Connection getConnection() {
        return connection;
    }

    public MySqlData getData() {
        return data;
    }

    @Override
    public void run() {
        while (true) {
            try {
                T obj = buffer.take();
                handle(obj);

            } catch (Exception e) {
                System.out.println("Connection failed!!!");
                e.printStackTrace();
            }
        }
    }

    protected abstract void handle(T object);

    protected abstract PreparedStatement getStatement(T object);
}
