package net.teamcrimx.partyandfriends.api.friends;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.kyori.adventure.util.TriState;
import net.teamcrimx.partyandfriends.api.constants.CloudConstants;
import net.teamcrimx.partyandfriends.api.database.MongoCollection;
import net.teamcrimx.partyandfriends.api.database.MongoDatabaseImpl;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SimpleFriend {

    private final UUID uuid;
    private final ArrayList<UUID> friends;
    private ArrayList<UUID> onlineFriends;
    private final ArrayList<UUID> friendRequests;
    private LoadingCache<UUID, TriState> onlineFriendsCache;

    public SimpleFriend(UUID uuid, ArrayList<UUID> friends, ArrayList<UUID> onlineFriends, ArrayList<UUID> friendRequests) {
        this.uuid = uuid;
        this.friends = friends;
        this.onlineFriends = onlineFriends;
        this.friendRequests = friendRequests;

        this.onlineFriendsCache = Caffeine.newBuilder()
                .maximumSize(128)
                .refreshAfterWrite(30, TimeUnit.SECONDS)
                .build(k -> CloudConstants.playerManager.onlinePlayer(k) != null ? TriState.TRUE : TriState.FALSE);

        for (UUID friend : this.friends) {
            this.onlineFriendsCache.put(friend, TriState.NOT_SET);
            this.onlineFriendsCache.refresh(friend);
        }
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

    public void update(boolean database) {
        this.onlineFriends = new ArrayList<>(friends.stream()
                .filter(id -> CloudConstants.playerManager.onlinePlayer(id) == null)
                .toList());

        if(database) {
            MongoDatabaseImpl.mongoMethodsUtil().insert(this.uuid, "friends", this.friends, MongoCollection.FRIENDS);
            MongoDatabaseImpl.mongoMethodsUtil().insert(this.uuid, "friendRequests", this.friendRequests, MongoCollection.FRIENDS);
        }

    }
}
