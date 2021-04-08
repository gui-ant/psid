package grp02;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import grp07.Measurement;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


//  CASO A DB NAO ESTEJA ACESSIVEL!!!!!!!


public class ConnectToMongo {
    private final MongoClient client;
    private MongoDatabase database;

    private final ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer = new ConcurrentHashMap<>();

    public ConnectToMongo(String sourceUri) {
        this.client = MongoClients.create(sourceUri);
        System.out.println("Connected to the database successfully");
    }

    public ConnectToMongo(String sourceUri, String sourceDatabase) {
        this(sourceUri);
        useDatabase(sourceDatabase);
    }

    private Measurement getLastObject(MongoCollection<Measurement> collection) {
        return collection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    private static String getCollectionName(MongoCollection<Measurement> collection) {
        return collection.getNamespace().getCollectionName();
    }

    public void useCollections(String[] collectionNames) {
        collectionsDataBuffer.clear();
        for (String col : collectionNames)
            collectionsDataBuffer.put(col, new LinkedBlockingQueue<>());
    }

    public void useDatabase(String db) {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        this.database = client.getDatabase(db).withCodecRegistry(pojoCodecRegistry);
        useAllCollections();
    }

    public ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> getFetchingSource() {
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
        new Thread(new MeasureFetcher()).start();
    }

    private MongoCollection<Measurement> getMeasureCollection(String collectionName) {
        return database.getCollection(collectionName, Measurement.class);
    }

    class MeasureFetcher extends Thread {

        private static final int SLEEP_TIME = 1000;

        @Override
        public void run() {

            while (true) {
                collectionsDataBuffer.forEach((collection, buffer) -> {
                    Measurement doc = getLastObject(getMeasureCollection(collection));
                    // TODO: Considerar a collection estar vazia,i.e. gerar doc = null
                    ObjectId lastId = doc.getId();
                    try {
                        System.out.println("Fetching " + collection + "...");

                        MongoCursor<Measurement> cursor = getMeasureCollection(collection).find(Filters.gt("_id", lastId)).iterator();

                        // Le os novos dados e adiciona-os ao buffer
                        while (cursor.hasNext()) {
                            doc = cursor.next();
                            System.out.println("Fetched: " + doc.getId());

                            InsertOneResult res = getMeasureCollection(collection).insertOne(doc);
                            System.out.println("Inserted: " + res.getInsertedId());
                        }
                        sleep(SLEEP_TIME);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
