public class Main {
    public static void main(String[] args) {
//        retirar comentario quando sensores disponiveis

        String[] collectionNames = {
                "sensort1",
                "sensort2",
                "sensorh1",
                "sensorh2",
                "sensorl1",
                "sensorl2",
        };
        ConnectToDBSID conn = new ConnectToDBSID("sid2021", "g07");
        conn.startFetching(collectionNames);
    }
}