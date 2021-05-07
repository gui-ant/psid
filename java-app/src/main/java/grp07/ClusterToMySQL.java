package grp07;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import common.CustomLogger;
import common.IniConfig;
import common.SysOutLogger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class ClusterToMySQL {
    private static final int CADENCE_SECONDS = 5;
    private static final String INI_FILE = "config.ini";
    private final IniConfig config;
    private CustomLogger log;

    public ClusterToMySQL() {
        this.config = new IniConfig(INI_FILE);
        this.log = new SysOutLogger();
    }


    void initialize() {
        config.useSection("params");
        String mongo_cluster_uri = config.get("mongo_cluster_uri");
        String mongo_cluster_db = config.get("mongo_cluster_db");
        String mysql_cloud_uri = config.get("mysql_cloud_uri");
        String mysql_local_uri = config.get("mysql_local_uri");
        String collectionNames = config.get("collections");

        try {
            ConnectToMongo sourceCluster = new ConnectToMongo(mongo_cluster_uri, mongo_cluster_db);

            Connection mysqlCloud = DriverManager.getConnection(mysql_cloud_uri, "aluno", "aluno");
            Connection mysqlLocal = DriverManager.getConnection(mysql_local_uri, "root", "");

            MongoToSQL targetMySql = new MongoToSQL(mysqlLocal, new SqlSender(mysqlCloud, mysqlLocal), CADENCE_SECONDS);

            sourceCluster.useCollections(collectionNames.split(","));
            sourceCluster.startFetching();

            targetMySql.serveSQL(sourceCluster.getFetchingSource());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLog(CustomLogger logger) {
        this.log = logger;
    }

    public static void main(String[] args) {
        /* Linhas adicionas para desabilitar logs do mongo.driver na consola */
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);

        new ClusterToMySQL().initialize();
    }
}
