package net.teamcrimx.partyandfriends.api.database;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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

    public @Nullable List<String> getStringArrayListFromDocumentSync(UUID uuid, MongoCollection mongoDBCollection, String key) {
        Document found = this.getDocument(uuid, mongoDBCollection);

        if (found != null) {
            return found.getList(key, String.class);
        }

        return null;
    }

    public void insert(@NotNull UUID uuid, @NotNull String key, @NotNull Object value, @NotNull MongoCollection mongoCollection) {
        Document toInsert = this.getDocument(uuid, mongoCollection);
        Document document = new Document(key, value);
        Bson updateOperation = new Document("$set", document);

        if (toInsert == null) {
            return;
        }

        this.mongoImpl.getCollection(mongoCollection.collectionName()).updateOne(toInsert, updateOperation);
    }

}

