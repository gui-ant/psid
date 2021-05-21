import common.MigrationMethod;
import grp07.CloudToCluster;
import grp07.ClusterToMySQL;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;
import grp07.MySqlData;
import org.slf4j.LoggerFactory;

public class Main {
    private static final String CONFIG_FILE = "config.ini";

    public static void main(String[] args) {
        /* Linhas adicionas para desabilitar logs do mongo.driver na consola */
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);
        new MySqlData("config.ini");
        new BrokerToMongo(CONFIG_FILE);
        new CloudToCluster(CONFIG_FILE);
//        new ClusterToMySQL(CONFIG_FILE, MigrationMethod.MQTT);
        new ClusterToMySQL(CONFIG_FILE, MigrationMethod.DIRECT);

    }
}
