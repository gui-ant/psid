import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ConnectToDBSID extends Thread {
    private final MongoClientURI uri = new MongoClientURI("mongodb://aluno:aluno@194.210.86.10/?authSource=admin&authMechanism=SCRAM-SHA-1");
    private String sourceDB;
    private String targetDB;
    private String sourceCollectionName;
    private String targetCollectionName;
    private MongoClient mongo;
    private MongoDatabase sourceMongoDb;
    private MongoDatabase targetMongoDb;
    private MongoCollection<Document> sourceCollection;
    private MongoCollection<Document> targetCollection;

    public ConnectToDBSID(String sourceDb, String targetDb, String sourceCollection, String targetCollection) {
        this.sourceDB = sourceDb;
        this.targetDB = targetDb;
        this.sourceCollectionName = sourceCollection;
        this.targetCollectionName = targetCollection;
    }

    private void connect() {
        mongo = new MongoClient(uri);

        System.out.println("Connected to the database successfully");

        // Accessing the database
        sourceMongoDb = mongo.getDatabase(sourceDB);
        targetMongoDb = mongo.getDatabase(targetDB);

        sourceCollection = sourceMongoDb.getCollection(sourceCollectionName);
        targetCollection = targetMongoDb.getCollection(targetCollectionName);
    }

    private Document getLastObject(MongoCollection<Document> collection) {
        return collection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    private MongoCursor<Document> getInitialCursor() {
        Document doc = getLastObject(sourceCollection);
        return sourceCollection.find(Filters.gte("_id", doc.get("_id"))).iterator();
    }

//    APENAS PARA TESTES!!!
//    private MongoCursor<Document> getUpdatedCursor() {
//        FindIterable<Document> iterable = sourceCollection.find();
//        iterable.skip((int) sourceCollection.count());
//        return iterable.iterator();
//    }

    private void insertBulk(List<Document> documents, boolean ordered) {
        InsertManyOptions options = new InsertManyOptions();
        if (!ordered) {
            options.ordered(false);
        }
        targetCollection.insertMany(documents, options);
    }

    private void fetchData() {
        Document doc = null;
        MongoCursor<Document> cursor = getInitialCursor();
        List<Document> documents = new ArrayList<>();
        while (true) {
            try {
                while (cursor.hasNext()) {
                    doc = cursor.next();
                    System.out.println("Source: " + doc); // lê da cloud
                    documents.add(doc);
                }
                cursor = sourceCollection.find(Filters.gt("_id", doc.get("_id"))).iterator();
                if(!documents.isEmpty()) {
                    insertBulk(documents, true);
                    System.out.println("ESCREVER!!!!");
                    documents.clear();
                }
                Thread.sleep(2000);
            } catch (InterruptedException interruptedException) {
//                interruptedException.printStackTrace();
                System.err.println("DEU ERRO!");
            }
        }
    }

    public void run() {
        connect();
        fetchData();
    }

}