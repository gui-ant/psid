import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;

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

    private static String getCollectionName(MongoCollection<Document> collection) {
        return collection.getNamespace().getCollectionName();
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
        private static final int SLEEP_TIME = 5000;


        public DocumentFetcher(MongoCollection<Document> collection, LinkedBlockingQueue<Document> buffer) {
            this.collection = collection;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            Document doc = getLastObject(collection);

            // TODO: Considerar a collection estar vazia,i.e. gerar doc = null
            ObjectId lastId = doc.getObjectId("_id");
            while (true) {
                try {
                    System.out.println("Fetching " + getCollectionName(collection) + "...");

                    MongoCursor<Document> cursor = collection.find(Filters.gt("_id", lastId)).iterator();

                    // Le os novos dados e adiciona-os ao buffer
                    while (cursor.hasNext()) {
                        doc = cursor.next();
                        lastId = doc.getObjectId("_id");
                        buffer.offer(doc);
                        System.out.println("Fetched: " + doc.get("_id"));
                    }
                    Thread.sleep(SLEEP_TIME);

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
