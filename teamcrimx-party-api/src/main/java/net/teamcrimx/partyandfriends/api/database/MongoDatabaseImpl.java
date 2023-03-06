package net.teamcrimx.partyandfriends.api.database;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

public class MongoDatabaseImpl {

    private static @Nullable MongoClient mongoClient = null;
    public static @Nullable MongoMethodsUtil mongoMethodsUtil;

    private static MongoDatabase networkDatabase;

    private static com.mongodb.client.MongoCollection<Document> friendCollection;

    public static void initializeDatabase() {
        try {
            mongoClient = MongoClients.create();

            networkDatabase = mongoClient.getDatabase(MongoCollection.DATABASE.collectionName());

            friendCollection = networkDatabase.getCollection(MongoCollection.FRIENDS.collectionName());
        } catch (MongoException ignored) {
            // TODO: logger
        }

        mongoMethodsUtil = new MongoMethodsUtil();

    }

    public static MongoDatabase networkDatabase() {
        return networkDatabase;
    }

    public static com.mongodb.client.MongoCollection<Document> friendCollection() {
        return friendCollection;
    }

    public static MongoMethodsUtil mongoMethodsUtil() {
        return mongoMethodsUtil;
    }
}
