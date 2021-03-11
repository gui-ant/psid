import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.sql.Connection;


public class MongoToSql extends Thread {
    //    private String srcMongoCollectionName;
    private MongoCollection<Document> srcMongoCollection;
    private Connection sqlConn;
    private SqlSender sender;
    //    private List<Document> buffer;
    private Document sendable;
    private Document lastSentDoc;

    public MongoToSql (MongoDatabase srcMongoDB, String srcMongoCollectionName, Connection sqlConn, SqlSender sender) {
//        this.buffer = new LinkedList<>();
        this.sender = sender;
//        this.srcMongoCollectionName = srcMongoCollectionName;
        this.srcMongoCollection = srcMongoDB.getCollection(srcMongoCollectionName);
        this.sqlConn = sqlConn;
    }

    private void fetchData() {
        // Obtem o ultimo registo da targetCollection
//        String lastId = getLastObjectSQLid();

        // Se a targetCollection estiver vazia, baseia-se no último _id da sourceCollection
//        if (lastId == null)
//        String lastId = (String)getLastObjectMongo().get("_id");
        sendable = getLastObjectMongo();

        // cursor
//        MongoCursor<Document> cursor = srcMongoCollection.find(Filters.gt("_id", lastId)).iterator();

        // Le os novos dados e adiciona-os a LL

    }

    private Document getLastObjectMongo() {
        return srcMongoCollection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    private void sendToSql() {
        if(! sendable.equals(lastSentDoc)) {
            sender.send(sqlConn, sendable);
        }
        lastSentDoc = sendable;
    }

    public void run() {
        while (true) {
            try {
                fetchData();
            } catch (Exception e) {
                System.err.println("Erro em fetchData()");
            }
            try {
                sendToSql();
            } catch (Exception e) {
                System.err.println("Erro em sendToSql()");
            }
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
