package grp02;

import grp07.*;

public class ConnectionMongoReplics {

    private static final String SOURCE_URI = "mongodb://aluno:aluno@194.210.86.10/?authSource=admin&authMechanism=SCRAM-SHA-1";
    private static final String TARGET_URI_ATLAS = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String SOURCE_DB = "sid2021";
    private static final String TARGET_DB = "g07";

    public static void main(String[] args) {

        String[] collectionNames = {"sensort1"};

        ConnectToMongo cloud = new ConnectToMongo(SOURCE_URI, SOURCE_DB);

        ConnectToMongo cluster_atlas = new ConnectToMongo(TARGET_URI_ATLAS, TARGET_DB);

        cloud.useCollections(collectionNames);
        cloud.startFetching();

        cluster_atlas.startPublishing(cloud.getFetchingSource());
    }

}
