package common;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoConnector {
    private final MongoClient client;
    private MongoDatabase database;

    public MongoConnector(String sourceUri, String db) {
        this.client = MongoClients.create(sourceUri);
        useDatabase(db);
    }

    public void useDatabase(String db) {
        CodecRegistry defaultCodecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        CodecRegistry providersCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry pojoCodecRegistry = fromRegistries(defaultCodecRegistry, providersCodecRegistry);

        this.database = client.getDatabase(db).withCodecRegistry(pojoCodecRegistry);
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getCurrentDb() {
        return database;
    }
}