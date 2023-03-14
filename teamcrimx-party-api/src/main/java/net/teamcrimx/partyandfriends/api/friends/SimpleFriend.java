package net.teamcrimx.partyandfriends.api.friends;

import net.teamcrimx.partyandfriends.api.constants.CloudConstants;
import net.teamcrimx.partyandfriends.api.database.MongoCollection;
import net.teamcrimx.partyandfriends.api.database.MongoDatabaseImpl;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SimpleFriend {

    private final UUID uuid;
    private final ArrayList<UUID> friends;
    private final ArrayList<UUID> onlineFriends;
    private final ArrayList<UUID> friendRequests;

    public SimpleFriend(UUID uuid, ArrayList<UUID> friends, ArrayList<UUID> onlineFriends, ArrayList<UUID> friendRequests) {
        this.uuid = uuid;
        this.friends = friends;
        this.onlineFriends = onlineFriends;
        this.friendRequests = friendRequests;
    }

    public UUID uuid() {
        return uuid;
    }

    public ArrayList<UUID> friends() {
        return friends;
    }

    public ArrayList<UUID> onlineFriends() {
        return onlineFriends;
    }

    public ArrayList<UUID> friendRequests() {
        return friendRequests;
    }

    public static CompletableFuture<@Nullable SimpleFriend> getSimpleFriendByUUID(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Document friendDocument = MongoDatabaseImpl.mongoMethodsUtil().getDocument(uuid, MongoCollection.FRIENDS);
            if(friendDocument == null) {
                return null;
            }

            List<UUID> allFriends = friendDocument.getList("friends", String.class)
                    .stream().map(UUID::fromString).toList();

            List<UUID> onlineFriends = allFriends.stream().filter(id -> CloudConstants.playerManager.onlinePlayer(id) == null)
                    .toList();

            List<UUID> requests = friendDocument.getList("friendRequests", String.class)
                    .stream().map(UUID::fromString).toList();

            return new SimpleFriend(uuid, new ArrayList<>(allFriends), new ArrayList<>(onlineFriends), new ArrayList<>(requests));
        });
    }

}
