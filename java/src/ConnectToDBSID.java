import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class ConnectToDBSID extends Thread {
    private String db1Name = "";
    private String db2Name = "";
    private String coll1Name = "";
    private String coll2Name = "";

    private final MongoClientURI uri = new MongoClientURI("mongodb://aluno:aluno@194.210.86.10/?authSource=admin&authMechanism=SCRAM-SHA-1");
    private MongoClient mongo;

    private MongoDatabase database;
    private MongoDatabase database_test;
    private MongoCollection<Document> collection;
    private MongoCollection<Document> collection_test;

    public ConnectToDBSID(String db1, String db2, String coll1, String coll2) {
        this.db1Name = db1;
        this.db2Name = db2;
        this.coll1Name = coll1;
        this.coll2Name = coll2;
    }

    private void connect() {
        mongo = new MongoClient(uri);

        System.out.println("Connected to the database successfully");

        // Accessing the database
        database = mongo.getDatabase(db1Name);
        database_test = mongo.getDatabase(db2Name);

        // Creating a collection
        System.out.println("Collection created successfully");
        // Retrieving a collection
        collection = database.getCollection(coll1Name);
        collection_test = database_test.getCollection(coll2Name);
        System.out.println("Collection myCollection selected successfully");
    }

    public void test1() {
        long colSize = collection.count();
        System.out.println("Collection size = " + colSize);

        Document a = collection.find().first();
        String js = a.toJson();
        String[] b = js.split(",");
        System.out.println(a);
        System.out.println(b[0]);
        System.out.println(b[1]);
        System.out.println(b[2]);
    }

    public void test2() {
        Document cl = collection.find().first();
        System.out.println(cl.getClass());
//        List<Document> docTest = collection.find().into(new ArrayList<Document>());
//        collection_test.insertMany(collection.find().into(docTest);

//        collection.find().batchSize(1000).forEach((Block<? super Document>) document -> collection_test.insertOne(document));
    }

    private Document getLastObject() {
        return collection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    private void fetchData2() {
        for (Document z : collection.find()) {
            try {
                System.out.println(z);
                collection_test.insertOne(z);
            } catch (MongoWriteException e) {
                System.err.println("Já existe, oh estupido!");
            }
        }
    }

    private MongoCursor<Document> getUpdatedCursor() {
        FindIterable<Document> iterable = collection.find();
        iterable.skip((int) collection.count());
        return iterable.iterator();
    }

    private void fetchData() {
        MongoCursor<Document> cursor = getUpdatedCursor();
        while (true) {
            try {
                System.out.println(cursor.next());
                collection_test.insertOne(cursor.next());
            } catch (Exception e) {
                try {
                    cursor = getUpdatedCursor();
                    Thread.sleep(2000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
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