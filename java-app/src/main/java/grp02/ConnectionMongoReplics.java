package grp02;

public class ConnectionMongoReplics {
    private static final String SOURCE_URI = "mongodb://aluno:aluno@194.210.86.10/?authSource=admin&authMechanism=SCRAM-SHA-1";
    private static final String TARGET_URI = "...";
    private static final String SOURCE_DB = "sid2021";
    private static final String TARGET_DB = "g02";

    public static void main(String[] args) {

        ConnectToMongo cloud = new ConnectToMongo(SOURCE_URI, SOURCE_DB);

        ConnectToMongo cluster = new ConnectToMongo(TARGET_URI, TARGET_DB);

        cloud.startFetching();
        //cluster.startPublishing(cloud.getFetchingSource());
    }
}
