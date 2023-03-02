package net.teamcrimx.partyandfriends.api.friends;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SimpleFriend {

    private final UUID uuid;
    private final Set<UUID> friends;
    private final ArrayList<UUID> onlineFriends;

    public SimpleFriend(UUID uuid, Set<UUID> friends, ArrayList<UUID> onlineFriends) {
        this.uuid = uuid;
        this.friends = friends;
        this.onlineFriends = onlineFriends;
    }

    public UUID uuid() {
        return uuid;
    }

    public Set<UUID> friends() {
        return friends;
    }

    public ArrayList<UUID> onlineFriends() {
        return onlineFriends;
    }

    public static CompletableFuture<SimpleFriend> getSimpleFriendByUUID(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {



        });
    }

}
