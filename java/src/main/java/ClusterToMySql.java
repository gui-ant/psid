public class ClusterToMySql {
    public static void main(String[] args) {
        ConnectToMongo cluster = new ConnectToMongo("");
        cluster.useDatabase("g07");
        cluster.useAllCollections();

        ConnectToMySql mysql = new ConnectToMySql();
        mysql.populate(cluster.getFetchingSource());
    }
}
