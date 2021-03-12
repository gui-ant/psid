import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import org.bson.Document;

import java.util.LinkedList;


//  CASO A DB NAO ESTEJA ACESSIVEL!!!!!!!


public class ConnectToDBSID {
    private final MongoClientURI uri = new MongoClientURI("mongodb://aluno:aluno@194.210.86.10/?authSource=admin&authMechanism=SCRAM-SHA-1");
    private final MongoClientURI uriAtlas = new MongoClientURI("mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority");
    private String sourceDB;
    private String targetDB;
    private MongoClient mongo;
    private MongoClient mongoAtlas;
    private MongoDatabase sourceMongoDb;
    private MongoDatabase targetMongoDb;
    private LinkedList<Document> buffer;
    private Document lastDoc;

    public ConnectToDBSID(String sourceDb, String targetDb) {
        this.sourceDB = sourceDb;
        this.targetDB = targetDb;
        this.buffer = new LinkedList<>();
    }

    private void connect() {
        mongo = new MongoClient(uri);
        mongoAtlas = new MongoClient(uriAtlas);

        System.out.println("Connected to the database successfully");

        // Accessing the database
        sourceMongoDb = mongo.getDatabase(sourceDB);
        //targetMongoDb = mongo.getDatabase(targetDB);
        targetMongoDb = mongoAtlas.getDatabase(targetDB);
    }

    private synchronized Document getLastObject(MongoCollection<Document> collection) {
        return collection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    private synchronized void insertBulk(MongoCollection targetCollection, boolean ordered) {
        System.out.println("Inserting on " + targetCollection.getNamespace().getCollectionName() + "...");
        InsertManyOptions options = new InsertManyOptions();
        if (!buffer.isEmpty())
            targetCollection.insertMany(buffer, options.ordered(ordered));
        buffer.clear();
    }

    private void fetchData(MongoCollection<Document> sourceCollection) {
        String srcCollectionName = sourceCollection.getNamespace().getCollectionName();
        MongoCollection<Document> targetCollection = targetMongoDb.getCollection(srcCollectionName);

        //lastDoc é o ponto de partida para o Cursor iterar
        if (lastDoc == null) lastDoc = getLastObject(sourceCollection);

        // Obtem os novos dados da sourceCollection (i.e. _id > ultimo registo da targetCollection)
        MongoCursor<Document> cursor = sourceCollection.find(Filters.gt("_id", lastDoc.get("_id"))).iterator();

        // Le os novos dados e adiciona-os a ArrayList
        synchronized (this) {
            while (cursor.hasNext()) {
                lastDoc = cursor.next();
                System.out.println("Source: " + lastDoc); // lê da cloud
                buffer.add(lastDoc);
            }
        }

        insertBulk(targetCollection, true);
    }

    public void fetch(String[] collections) {
        connect();

        // Para cada collection lança uma thread
        for (String collName : collections) {
            new Thread(() -> {
                while (true) {
                    try {
                        System.out.println("Fetching " + collName + "...");
                        fetchData(sourceMongoDb.getCollection(collName));
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
