package common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public interface ClientToClient<T> {
    void startFetching();

    void startPublishing();

    ConcurrentHashMap<String, LinkedBlockingQueue<T>> getBuffer();

}
