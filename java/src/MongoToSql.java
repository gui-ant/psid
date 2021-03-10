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
        String lastId = getLastObjectSQLid();

        // Se a targetCollection estiver vazia, baseia-se no último _id da sourceCollection
        if (lastId == null)
            lastId = getLastObjectMongo().get("_id").toString();

        // cursor
        MongoCursor<Document> cursor = srcMongoCollection.find(Filters.gt("_id", lastId)).iterator();

        // Le os novos dados e adiciona-os a LL
        while (cursor.hasNext()) {
            buffer.add(cursor.next());
        }
    }

    private String getLastObjectSQLid() {

        //seleciona a última linha da tabela measures
        String q = "SELECT max(id) FROM measures";

        Statement statement;
        ResultSet result;
        String last_id;

        try {
            statement = sqlConn.prepareStatement(q);
            System.out.println(statement);
            result = statement.executeQuery(q);

            //Se a query vier vazia o result.next() retorna false
            if(result.next()){

                //columnIndex 1 é a coluna do ID. Por columnName estava a dar erro ¯\_(ツ)_/¯
                last_id = result.getString(1);
                System.out.println("ID da última entrada: " + last_id);

                return last_id;
            }
            else {
                System.out.println("[MongoToSql]: Tabela measures vazia");
                return null;
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        System.err.println("Erro[MongoToSql]: passou o try/catch");
        return null;

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
