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
                System.out.println("To insert: " + obj);

                PreparedStatement stmt = getStatement(obj);

                if (stmt.execute())
                    System.out.println("Inserted value successfully!!!");

                stmt.close();

            } catch (Exception e) {
                System.out.println("Connection failed!!!");
                e.printStackTrace();
            }
        }

    }

    protected abstract PreparedStatement getStatement(T object);
}
