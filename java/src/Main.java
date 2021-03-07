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
        ConnectToDBSID t1 = new ConnectToDBSID("sid2021", "g07");
        t1.fetch(collectionNames);
    }
}