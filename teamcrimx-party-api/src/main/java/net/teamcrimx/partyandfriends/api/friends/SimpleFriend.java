package net.teamcrimx.partyandfriends.api.friends;

import java.util.UUID;

public class SimpleFriend {

    private final UUID uuid;

    public SimpleFriend(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() {
        return uuid;
    }
}
