import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class MongoToSql extends Thread {
    private String srcMongoCollectionName;
    private MongoCollection<Document> srcMongoCollection;
    private Connection sqlConn;
    private SqlSender sender;
    private List<Document> buffer;

    public MongoToSql (MongoDatabase srcMongoDB, String srcMongoCollectionName, Connection sqlConn, SqlSender sender) {
        this.buffer = new LinkedList<>();
        this.sender = sender;
        this.srcMongoCollectionName = srcMongoCollectionName;
        this.srcMongoCollection = srcMongoDB.getCollection(srcMongoCollectionName);
        this.sqlConn = sqlConn;
    }

    private void fetchData() throws SQLException {
        // Obtem o ultimo registo da targetCollection
        Object lastId = getLastObjectSQLid();

        // Se a targetCollection estiver vazia, baseia-se no último _id da sourceCollection
        if (lastId == null) {
            lastId = getLastObjectMongo().get("_id");
        }
        else {
            lastId = (ObjectId)lastId;
        }

        // cursor
        MongoCursor<Document> cursor = srcMongoCollection.find(Filters.gt("_id", lastId)).iterator();

        // Le os novos dados e adiciona-os a LL
        while (cursor.hasNext()) {
            buffer.add(cursor.next());
        }
    }

    private Object getLastObjectSQLid() {
//        String q = "SELECT MAX(id) FROM measures WHERE sensor_id = " + srcMongoCollectionName;
//        String q = "SELECT MAX(id) FROM measures WHERE sensor_id = " + '1';
        String q = "SELECT * FROM users WHERE email = 'pajo@iscte.pt'";
//        String q = "SELECT * FROM users";
        Statement statement;
        ResultSet result;
        Object obj = null;
        try {
            statement = sqlConn.prepareStatement(q);
            System.out.println("TESTE!!!!!!!! " + statement);
            result = statement.executeQuery(q);

            while (result.next()) {
                System.out.println("NAME: " + result.getString("name"));
                System.out.println("EMAIL: " + result.getString("email"));
                System.out.println("ID: " + result.getString("id"));
            }

            obj = result;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return obj;
    }

    private Document getLastObjectMongo() {
        return srcMongoCollection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    private void sendToSql() {
        sender.send(sqlConn, buffer);
        buffer.clear();
    }

    public void run() {
        while (true) {
            try {
                fetchData();
            } catch (Exception e) {
                System.err.println("Erro em fetchData()");
            }
//            try {
//                sendToSql();
//            } catch (Exception e) {
//                System.err.println("Erro em sendToSql()");
//            }
        }
    }

}
