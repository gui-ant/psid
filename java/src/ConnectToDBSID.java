import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

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
        return sourceCollection.find(Filters.gt("_id", doc.get("_id"))).iterator();
    }

    private void fetchData() {
        Document doc = null;
        MongoCursor<Document> cursor = getInitialCursor();
        while (true) {
            try {
                while (cursor.hasNext()) {
                    doc = cursor.next();
                    System.out.println("Source: " + doc); // lê da cloud
                    targetCollection.insertOne(doc); // regista na g07
                }
                cursor = sourceCollection.find(Filters.gt("_id", doc.get("_id"))).iterator();
                Thread.sleep(2000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }

    public void run() {
        connect();
        fetchData();
    }

}