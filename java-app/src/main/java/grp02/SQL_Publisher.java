package grp02;

import grp07.Measurement;
import grp07.SqlSender;

import java.sql.Connection;
import java.util.concurrent.LinkedBlockingQueue;

public class SQL_Publisher extends Thread{

    private final Connection connection;
    private final SqlSender sender;
    private final LinkedBlockingQueue<Measurement> buffer;

    public SQL_Publisher(Connection connection, SqlSender sender, LinkedBlockingQueue<Measurement> buffer) {
        this.connection = connection;
        this.sender = sender;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            while(true) {
                Measurement measurement = buffer.take();
                sender.send(connection, measurement, true);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
