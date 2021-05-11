package grp07;

public class CloudToCluster {
    private static final String SOURCE_URI_ATLAS = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String TARGET_URI_CLUSTER = "mongodb://127.0.0.1:27017";
    //private static final String TARGET_URI_MADRUGADAO = "mongodb://aluno:aluno@madrugadao-sama.ddns.net/g07?authSource=admin&authMechanism=SCRAM-SHA-1";
    private static final String SOURCE_DB = "g07";
    private static final String TARGET_DB = "g07";

    public static void main(String[] args) {

        String[] collectionNames = {
                "sensort1",
                "sensort2",
        };

        ConnectToMongo cloud = new ConnectToMongo(SOURCE_URI_ATLAS, SOURCE_DB);
        ConnectToMongo cloud2 = new ConnectToMongo(SOURCE_URI_ATLAS, SOURCE_DB);

        ConnectToMongo cluster_atlas = new ConnectToMongo(TARGET_URI_CLUSTER, TARGET_DB);
        //ConnectToMongo cluster_madrugadao = new ConnectToMongo(TARGET_URI_MADRUGADAO, TARGET_DB);

        cloud.useCollections(collectionNames); // Se omitido, usa todas as coleções da DB
        cloud2.useCollections(collectionNames); // Se omitido, usa todas as coleções da DB

        cloud.startFetching();
        cloud2.startFetching();

        cluster_atlas.startPublishing(cloud.getFetchingSource());
        //cluster_madrugadao.startPublishing(cloud2.getFetchingSource());
    }
}