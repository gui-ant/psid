public class CloudToCluster {
    private static final String SOURCE_URI = "mongodb://aluno:aluno@194.210.86.10/?authSource=admin&authMechanism=SCRAM-SHA-1";
    private static final String TARGET_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String SOURCE_DB = "sid2021";
    private static final String TARGET_DB = "g07";

    public static void main(String[] args) {

        String[] collectionNames = {
                "sensort1",
                "sensort2",
        };

        ConnectToMongo cloud = new ConnectToMongo(SOURCE_URI, SOURCE_DB);
        ConnectToMongo cluster = new ConnectToMongo(TARGET_URI, TARGET_DB);

        cloud.useCollections(collectionNames); // Se omitido, usa todas as coleções da DB
        cloud.startFetching();

        cluster.startPublishing(cloud.getFetchingSource());
    }
}