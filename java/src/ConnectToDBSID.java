import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import org.bson.Document;

import java.util.LinkedList;
import java.util.List;


//  CASO A DB NAO ESTEJA ACESSIVEL!!!!!!!


public class ConnectToDBSID {
    private static final String SOURCE_URI = "mongodb://aluno:aluno@194.210.86.10/?authSource=admin&authMechanism=SCRAM-SHA-1";
    private static final String TARGET_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";

    private final MongoDatabase sourceMongoDb;
    private final MongoDatabase targetMongoDb;

    private final LinkedList<Document> buffer = new LinkedList<>();

    public ConnectToDBSID(String sourceDb, String targetDb) {
        MongoClientURI sourceURI = new MongoClientURI(SOURCE_URI);
        MongoClientURI targetURI = new MongoClientURI(TARGET_URI);

        System.out.println("Connected to the database successfully");

        // Accessing the database
        this.sourceMongoDb = new MongoClient(sourceURI).getDatabase(sourceDb);
        //targetMongoDb = mongo.getDatabase(targetDB);
        this.targetMongoDb = new MongoClient(targetURI).getDatabase(targetDb);
    }

    private String getCollectionName(MongoCollection collection) {
        return collection.getNamespace().getCollectionName();
    }

    private Document getLastObject(MongoCollection<Document> collection) {
        return collection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    private void insertBulk(List data, MongoCollection<Document> targetCollection, boolean ordered) {
        System.out.println("Inserting on " + getCollectionName(targetCollection) + "...");

        if (!data.isEmpty())
            targetCollection.insertMany(data, new InsertManyOptions().ordered(ordered));
    }

    private List fetchData(MongoCollection<Document> sourceCollection) {
        List buffer = new LinkedList<>();

        // Obtem o ultimo registo da targetCollection
        MongoCollection<Document> targetCollection = targetMongoDb.getCollection(getCollectionName(sourceCollection));
        Document doc = getLastObject(targetCollection);

        // Se a targetCollection estiver vazia, baseia-se no último _id da sourceCollection
        Object lastId = doc != null ? doc.get("_id") : getLastObject(sourceCollection).get("_id");

        // Obtem os novos dados da sourceCollection (i.e. _id > ultimo registo da targetCollection)
        MongoCursor<Document> cursor = sourceCollection.find(Filters.gt("_id", lastId)).iterator();

        // Le os novos dados e adiciona-os a ArrayList
        while (cursor.hasNext()) {
            doc = cursor.next();
            System.out.println("Source: " + doc); // lê da cloud
            buffer.add(doc);
        }

        return buffer;
    }

    public void startFetching(String[] collections) {
        // Para cada collection lança uma thread
        for (String collName : collections) {
            new Thread(() -> {
                while (true) {
                    try {
                        System.out.println("Fetching " + collName + "...");

                        MongoCollection<Document> sourceCollection = sourceMongoDb.getCollection(collName);
                        MongoCollection<Document> targetCollection = targetMongoDb.getCollection(getCollectionName(sourceCollection));

                        List data = fetchData(sourceCollection);
                        insertBulk(data, targetCollection, true);

                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
