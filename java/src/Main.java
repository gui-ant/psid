public class Main {
    public static void main (String[] args) {
        ConnectToDBSID sid = new ConnectToDBSID("sid2021", "g07", "sensort1", "col1");
        sid.start();
//        ConnectToDBSID sid = new ConnectToDBSID("g07", "g07", "col1", "col1");
//        sid.start();
//        ConnectToDBSID sid2 = new ConnectToDBSID("sid2021", "g07", "sensort1", "col2");
//        sid2.start();
    }
}