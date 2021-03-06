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
    private final MongoClientURI URI = new MongoClientURI("mongodb://aluno:aluno@194.210.86.10/?authSource=admin&authMechanism=SCRAM-SHA-1");

    private final String sourceDb;
    private final String targetDb;
    private final String srcCollectionName;
    private final String trgCollectionName;

    private MongoClient mongo;
    private MongoCollection<Document> sourceCollection;
    private MongoCollection<Document> targetCollection;

    public ConnectToDBSID(String sourceDb, String targetDb, String sourceCollection, String targetCollection) {
        this.sourceDb = sourceDb;
        this.targetDb = targetDb;
        this.srcCollectionName = sourceCollection;
        this.trgCollectionName = targetCollection;
    }

    private void connect() {
        mongo = new MongoClient(URI);

        System.out.println("Connected to the database successfully");

        sourceCollection = getDatabase(sourceDb).getCollection(srcCollectionName);
        targetCollection = getDatabase(targetDb).getCollection(trgCollectionName);
    }

    private MongoDatabase getDatabase(String databaseName) {
        return mongo.getDatabase(databaseName);
    }

    private Document getLastObject(MongoCollection<Document> collection) {
        return collection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    /*private MongoCursor<Document> getInitialCursor() {
        Document doc = getLastObject(sourceCollection);
        return sourceCollection.find(Filters.gte("_id", doc.get("_id"))).iterator();
    }*/

//    APENAS PARA TESTES!!!
//    private MongoCursor<Document> getUpdatedCursor() {
//        FindIterable<Document> iterable = sourceCollection.find();
//        iterable.skip((int) sourceCollection.count());
//        return iterable.iterator();
//    }

    private void insertBulk(List<Document> documents, boolean ordered) {
        if (!documents.isEmpty())
            targetCollection.insertMany(documents, new InsertManyOptions().ordered(ordered));
    }

    private void fetchData() {
        List<Document> documents = new ArrayList<>();
        MongoCursor<Document> cursor;

        while (true) {
            try {
                // Obtem o ultimo registo da targetCollection
                Document doc = getLastObject(targetCollection);

                // Se a targetCollection estiver vazia, baseia-se no último _id da sourceCollection
                Object lastId = doc != null ? doc.get("_id") : getLastObject(sourceCollection).get("_id");

                // Obtem os novos dados da sourceCollection (i.e. _id > ultimo registo da targetCollection)
                cursor = sourceCollection.find(Filters.gt("_id", lastId)).iterator();

                // Le os novos dados e adiciona-os a ArrayList
                while (cursor.hasNext()) {
                    doc = cursor.next();
                    System.out.println("Source: " + doc);
                    documents.add(doc);
                }

                // Insere na targetDb, os dados da ArrayList
                insertBulk(documents, true);
                System.out.println("ESCREVER!!!!");
                documents.clear();

                Thread.sleep(2000);
            } catch (InterruptedException interruptedException) {
                //interruptedException.printStackTrace();
                System.err.println("DEU ERRO!");
            }

        }
    }

    public void run() {
        connect();
        fetchData();
    }
}