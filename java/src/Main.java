public class Main {
    public static void main (String[] args) {
//        retirar comentario quando sensores disponiveis

//        ConnectToDBSID h1 = new ConnectToDBSID("sid2021", "g07", "sensorh1", "sensorh1");
//        h1.start();
//        ConnectToDBSID h2 = new ConnectToDBSID("sid2021", "g07", "sensorh2", "sensorh2");
//        h2.start();
//        ConnectToDBSID l1 = new ConnectToDBSID("sid2021", "g07", "sensorl1", "sensorl1");
//        l1.start();
//        ConnectToDBSID l2 = new ConnectToDBSID("sid2021", "g07", "sensorl2", "sensorl2");
//        l2.start();
        ConnectToDBSID t1 = new ConnectToDBSID("sid2021", "g07");
        t1.init();
//        ConnectToDBSID t2 = new ConnectToDBSID("sid2021", "g07", "sensort2", "sensort2");
//        t2.start();
    }
}