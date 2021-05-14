import grp07.MySqlData;

public class Main {
    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";

    private static final String MONGO_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority"; // Atlas
    private static final String MONGO_DB = "g07";

    private static final String MYSQL_URI = "jdbc:mysql://localhost:3306/g07_local";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "";

    private static final int MONGO_TO_SQL_SLEEP_TIME = 5000;
    private static final String[] COLLECTIONS = {"sensort1", "sensort2"};

    public static void main(String[] args) {
        new MySqlData();
    }
}
