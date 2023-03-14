package net.teamcrimx.partyandfriends.cloud.friends.manager;

import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FriendHolder {

    private final Map<UUID, SimpleFriend> simpleFriendMap;

    public FriendHolder() {
        this.simpleFriendMap = new HashMap<>();
    }

    public Map<UUID, SimpleFriend> simpleFriendMap() {
        return simpleFriendMap;
    }
}
