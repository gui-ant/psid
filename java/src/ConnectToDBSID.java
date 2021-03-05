import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
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

    public void test1() {
        long colSize = sourceCollection.count();
        System.out.println("Collection size = " + colSize);

        Document a = sourceCollection.find().first();
        String js = a.toJson();
        String[] b = js.split(",");
        System.out.println(a);
        System.out.println(b[0]);
        System.out.println(b[1]);
        System.out.println(b[2]);
    }

    public void test2() {
        Document cl = sourceCollection.find().first();
        System.out.println(cl.getClass());
//        List<Document> docTest = collection.find().into(new ArrayList<Document>());
//        collection_test.insertMany(collection.find().into(docTest);

//        collection.find().batchSize(1000).forEach((Block<? super Document>) document -> collection_test.insertOne(document));
    }

    private Document getLastObject() {
        return sourceCollection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    private void fetchData2() {
        for (Document z : sourceCollection.find()) {
            try {
                System.out.println(z);
                targetCollection.insertOne(z);
            } catch (MongoWriteException e) {
                System.err.println("Já existe, oh estupido!");
            }
        }
    }

    private MongoCursor<Document> getUpdatedCursor() {
        FindIterable<Document> data = sourceCollection.find();
        data.skip((int) sourceCollection.count());
        return data.iterator();
    }

    private void fetchData() {
        Document doc = null;
        MongoCursor<Document> cursor = getUpdatedCursor();
        ;
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


//        NAO APAGAR!!!
//        collection.find( new BasicDBObject("_id", new ObjectId("603fbc3f9d4da9b7e728d4b0")));

    }

}