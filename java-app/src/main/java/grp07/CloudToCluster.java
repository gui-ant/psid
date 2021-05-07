package grp07;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;
import common.CustomLogger;
import common.IniConfig;
import common.SysOutLogger;
import org.slf4j.LoggerFactory;

public class CloudToCluster {
    private static final String INI_FILE = "config.ini";
    private IniConfig config;
    private CustomLogger log;

    public CloudToCluster() {
        this.config = new IniConfig(INI_FILE);
        this.log = new SysOutLogger();
    }

    public void initialize() {
        config.useSection("params");
        String mongo_cloud_uri = config.get("mongo_cloud_uri");
        String mongo_cloud_db = config.get("mongo_cloud_db");
        String mongo_cluster_uri = config.get("mongo_cluster_uri");
        String mongo_cluster_db = config.get("mongo_cluster_db");
        String collectionNames = config.get("collections");

        ConnectToMongo cloud = new ConnectToMongo(mongo_cloud_uri, mongo_cloud_db);
        ConnectToMongo cloud2 = new ConnectToMongo(mongo_cloud_uri, mongo_cloud_db);

        cloud.setLog(this.log);
        cloud2.setLog(this.log);

        ConnectToMongo cluster_atlas = new ConnectToMongo(mongo_cluster_uri, mongo_cluster_db);

        cloud.useCollections(collectionNames.split(",")); // Se omitido, usa todas as coleções da DB
        cloud2.useCollections(collectionNames.split(",")); // Se omitido, usa todas as coleções da DB

        cloud.startFetching();
        cloud2.startFetching();

        cluster_atlas.startPublishing(cloud.getFetchingSource());
    }

    public void setLog(CustomLogger log) throws Exception {
        this.log = log;
    }

    public static void main(String[] args) {
        /* Linhas adicionas para desabilitar logs do mongo.driver na consola */
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        new CloudToCluster().initialize();
    }

}
