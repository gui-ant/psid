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

//    private String getLastObjectSQLid() {
//
//        //seleciona a última linha da tabela measures
//        String q = "SELECT max(id) FROM measures";
//
//        Statement statement;
//        ResultSet result;
//        String last_id;
//
//        try {
//            statement = sqlConn.prepareStatement(q);
//            System.out.println(statement);
//            result = statement.executeQuery(q);
//
//            //Se a query vier vazia o result.next() retorna false
//            if(result.next()){
//
//                System.out.println("[MongoToSql]:Apanhou entrada sql");
//                //columnIndex 1 é a coluna do ID. Por columnName estava a dar erro ¯\_(ツ)_/¯
//                last_id = result.getString(1);
//
//                return last_id;
//            }
//            else {
//                System.err.println("Erro[MongoToSql]: Tabela measures vazia");
//                return null;
//            }
//
//        } catch (SQLException sqlException) {
//            sqlException.printStackTrace();
//        }
//
//        System.err.println("Erro[MongoToSql]: passou o try/catch");
//        return null;
//
//    }

    private Document getLastObjectMongo() {
        return srcMongoCollection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    private void sendToSql() {
        sender.send(sqlConn, sendable);
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
