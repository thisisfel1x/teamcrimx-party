package net.teamcrimx.partyandfriends.api.friends;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.util.TriState;
import net.teamcrimx.partyandfriends.api.NetworkPlayer;
import net.teamcrimx.partyandfriends.api.constants.CloudConstants;
import net.teamcrimx.partyandfriends.api.database.MongoCollection;
import net.teamcrimx.partyandfriends.api.database.MongoDatabaseImpl;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SimpleFriend extends NetworkPlayer {

    private final String name;
    private final UUID uuid;
    private final ArrayList<UUID> friends;
    private final ArrayList<UUID> friendRequests;
    private final PlayerManager playerManager;
    private ArrayList<UUID> onlineFriends;
    private LoadingCache<UUID, TriState> onlineFriendsCache;

    public SimpleFriend(String name, UUID uuid, ArrayList<UUID> friends, ArrayList<UUID> onlineFriends, ArrayList<UUID> friendRequests) {
        super(uuid);
        this.name = name;
        this.uuid = uuid;
        this.friends = friends;
        this.onlineFriends = onlineFriends;
        this.friendRequests = friendRequests;

        this.playerManager = ServiceRegistry.first(PlayerManager.class);

        this.onlineFriendsCache = Caffeine.newBuilder()
                .maximumSize(128)
                .refreshAfterWrite(10, TimeUnit.SECONDS)
                .build(k -> this.playerManager.onlinePlayer(k) != null ? TriState.TRUE : TriState.FALSE);

        for (UUID friend : this.friends) {
            this.onlineFriendsCache.put(friend, TriState.NOT_SET);
            this.onlineFriendsCache.refresh(friend);
        }
    }

    public static CompletableFuture<@Nullable SimpleFriend> getSimpleFriendByUUID(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String name = CloudConstants.playerManager.onlinePlayer(uuid).name();

            Document friendDocument = MongoDatabaseImpl.mongoMethodsUtil().getDocument(uuid, MongoCollection.FRIENDS);
            if (friendDocument == null) {
                return null;
            }

            List<UUID> allFriends = friendDocument.getList("friends", String.class)
                    .stream().map(UUID::fromString).toList();

            List<UUID> onlineFriends = allFriends.stream().filter(id -> CloudConstants.playerManager.onlinePlayer(id) != null)
                    .toList();

            List<UUID> requests = friendDocument.getList("friendRequests", String.class)
                    .stream().map(UUID::fromString).toList();

            return new SimpleFriend(name, uuid, new ArrayList<>(allFriends),
                    new ArrayList<>(onlineFriends), new ArrayList<>(requests));
        });
    }

    public String name() {
        return name;
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

    public LoadingCache<UUID, TriState> onlineFriendsCache() {
        return onlineFriendsCache;
    }

    public void update(boolean database) {
        this.onlineFriends = new ArrayList<>(friends.stream()
                .filter(id -> this.playerManager.onlinePlayer(id) != null)
                .toList());

        for (UUID friend : this.friends) {
            if (this.onlineFriendsCache.getIfPresent(friend) == null) {
                this.onlineFriendsCache.put(friend, TriState.NOT_SET);
            }
            this.onlineFriendsCache.refresh(friend);
        }

        if (database) {
            MongoDatabaseImpl.mongoMethodsUtil().insert(this.uuid, "friends",
                    this.friends.stream().map(UUID::toString).collect(Collectors.toList()), MongoCollection.FRIENDS);
            MongoDatabaseImpl.mongoMethodsUtil().insert(this.uuid, "friendRequests",
                    this.friendRequests.stream().map(UUID::toString).collect(Collectors.toList()), MongoCollection.FRIENDS);
        }
    }
}
