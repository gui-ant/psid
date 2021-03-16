import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


//  CASO A DB NAO ESTEJA ACESSIVEL!!!!!!!


public class ConnectToMongo {
    private final MongoClient client;
    private MongoDatabase database;

    private final ConcurrentHashMap<String, LinkedBlockingQueue<Document>> collectionsDataBuffer = new ConcurrentHashMap<>();

    public ConnectToMongo(String sourceUri) {
        this.client = MongoClients.create(sourceUri);
        System.out.println("Connected to the database successfully");
    }

    public ConnectToMongo(String sourceUri, String sourceDatabase) {
        this(sourceUri);
        useDatabase(sourceDatabase);
    }

    private Document getLastObject(MongoCollection<Document> collection) {
        return collection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    public void useCollections(String[] collectionNames) {
        collectionsDataBuffer.clear();
        for (String col : collectionNames)
            collectionsDataBuffer.put(col, new LinkedBlockingQueue<>());
    }

    public void useDatabase(String db) {
        this.database = client.getDatabase(db);
        useAllCollections();
    }

    public ConcurrentHashMap<String, LinkedBlockingQueue<Document>> getFetchingSource() {
        return collectionsDataBuffer;
    }

    // Considera todas as coleções da database
    public void useAllCollections() {
        List<String> collectionNames = new ArrayList<>();
        database.listCollectionNames().map(collectionNames::add);

        String[] arr = new String[collectionNames.size()];
        useCollections(collectionNames.toArray(arr));
    }

    public void startFetching() {
        // Para cada collection lança uma thread
        collectionsDataBuffer.forEach((collection, buffer) -> {
            MongoCollection<Document> sourceCollection = database.getCollection(collection);
            //LinkedBlockingQueue<Document> sourceBuffer = collectionsDataBuffer.get(getCollectionName(sourceCollection));
            new Thread(new DocumentFetcher(sourceCollection, buffer)).start();
        });
    }

    public void startPublishing(ConcurrentHashMap<String, LinkedBlockingQueue<Document>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> {
                    Runnable publisher = new DocumentPublisher(database.getCollection(collectionName), buffer);
                    new Thread(publisher).start();
                }
        );
    }

    class DocumentFetcher implements Runnable {

        private final MongoCollection<Document> collection;
        private final LinkedBlockingQueue<Document> buffer;

        public DocumentFetcher(MongoCollection<Document> collection, LinkedBlockingQueue<Document> buffer) {
            this.collection = collection;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            Document doc = getLastObject(collection);
            int minSleepingTime = 1000;
            double force = 0.1;
            int results = 0;
            while (true) {
                try {
                    // Le os novos dados e adiciona-os ao buffer
                    for (Document document : collection.find(Filters.gt("_id", doc.get("_id")))) {
                        doc = document;
                        buffer.offer(doc);
                        System.out.println("Fetched: " + doc.get("_id"));
                        results++;
                    }
                    System.out.println((long) ((double) minSleepingTime * (1 + (double) (results - 1) * force)));
                    Thread.sleep((long) ((double) minSleepingTime * (1 + (double) (results) * force)));
                    results = 0;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class DocumentPublisher implements Runnable {
        private final MongoCollection<Document> collection;
        private final LinkedBlockingQueue<Document> buffer;

        DocumentPublisher(MongoCollection<Document> collection, LinkedBlockingQueue<Document> buffer) {
            this.collection = collection;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    InsertOneResult res = collection.insertOne(buffer.take());
                    System.out.println("Inserted: " + res.getInsertedId());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
