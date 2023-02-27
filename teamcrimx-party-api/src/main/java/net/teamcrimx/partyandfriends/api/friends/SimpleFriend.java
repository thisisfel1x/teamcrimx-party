package net.teamcrimx.partyandfriends.api.friends;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SimpleFriend {

    private final UUID uuid;
    private Set<UUID> friends = new HashSet<>();
    private final ArrayList<UUID> onlineFriends = new ArrayList<>();

    public SimpleFriend(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() {
        return uuid;
    }

    public Set<UUID> friends() {
        return friends;
    }

    public void friends(Set<UUID> friends) {
        this.friends = friends;
    }

    public ArrayList<UUID> onlineFriends() {
        return onlineFriends;
    }
}
