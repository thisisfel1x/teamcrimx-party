package net.teamcrimx.partyandfriends.api.database;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MongoMethodsUtil {

    private final MongoDatabase mongoImpl;

    public MongoMethodsUtil() {
        mongoImpl = MongoDatabaseImpl.networkDatabase();
    }

    public boolean doesExists(UUID uuid, MongoCollection mongoCollection) {
        return this.mongoImpl.getCollection(mongoCollection.collectionName())
                .find(new Document("_id", uuid.toString())).first() != null;
    }

    public void insertDocumentSync(Document document, MongoCollection mongoCollection) {
        this.mongoImpl.getCollection(mongoCollection.collectionName()).insertOne(document);
    }

    public @Nullable Document getDocument(UUID uuid, MongoCollection mongoCollection) {
        return this.mongoImpl.getCollection(mongoCollection.collectionName())
                .find(new Document("_id", uuid.toString())).first();
    }

}

