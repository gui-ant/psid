package grp02;

import grp07.Measurement;
import grp07.SqlSender;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;


public class ConnectionSQL {

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "t_sensores";// nome na especificação
    private static final int QOS = 0;

    private static final String MYSQL_URL_LOCAL = "jdbc:mysql://194.210.86.10:3306/aluno_g07_local";
    private static final String MYSQL_URL_CLOUD = "jdbc:mysql://194.210.86.10:3306/aluno_g07_cloud";

    private LinkedBlockingQueue<Measurement> buffer = new LinkedBlockingQueue<Measurement>();

    public static void main(String[] args) throws MqttException, SQLException {

        BrokerSubscriber subscriber = new BrokerSubscriber(BROKER_URI, TOPIC, QOS);

        final Connection mysql_cloud = DriverManager.getConnection(MYSQL_URL_CLOUD, "aluno", "aluno");
        final Connection mysql_local = DriverManager.getConnection(MYSQL_URL_LOCAL, "aluno", "aluno");
        SqlSender sender = new SqlSender(mysql_cloud, mysql_local);

        SQL_Publisher publisher = new SQL_Publisher(mysql_local, sender, subscriber.getBuffer());
        publisher.start();
    }

}
