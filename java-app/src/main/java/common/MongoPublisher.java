package common;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import grp07.Measurement;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class MongoPublisher<T> extends Thread {
    private final MongoCollection<T> collection;
    private final LinkedBlockingQueue<T> buffer;

    public MongoPublisher(MongoCollection<T> collection, LinkedBlockingQueue<T> buffer) {
        this.collection = collection;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                InsertOneResult res = this.collection.insertOne(buffer.take());
                System.out.println("Inserted: " + res.getInsertedId());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}