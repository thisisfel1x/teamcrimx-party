package net.teamcrimx.partyandfriends.api.database;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

public class MongoDatabaseImpl {

    private static @Nullable MongoClient mongoClient = null;

    private static MongoDatabase networkDatabase;

    private static MongoCollection<Document> friendCollection;

    public static void initializeDatabase() {
        try {
            mongoClient = MongoClients.create();

            networkDatabase = mongoClient.getDatabase(MongoCollections.DATABASE.collecitonName());

            friendCollection = networkDatabase.getCollection(MongoCollections.FRIENDS.collecitonName());
        } catch (MongoException ignored) {
            // TODO: logger
        }

    }

    public static MongoDatabase networkDatabase() {
        return networkDatabase;
    }

    public static MongoCollection<Document> friendCollection() {
        return friendCollection;
    }
}
