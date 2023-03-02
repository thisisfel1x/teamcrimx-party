package net.teamcrimx.partyandfriends.cloud.manager;

import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.teamcrimx.partyandfriends.api.constants.ChatConstants;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;
import net.teamcrimx.partyandfriends.cloud.SimpleManager;

import java.util.UUID;

public class FriendManager extends SimpleManager {

    private final PartyAndFriendsModule partyAndFriendsModule;

    private final Component friendPrefix = ChatConstants.friendPrefix;

    public FriendManager(PartyAndFriendsModule partyAndFriendsModule) {
        super(partyAndFriendsModule);
        this.partyAndFriendsModule = partyAndFriendsModule;

        this.prefix(friendPrefix);
    }

    public void addFriend(UUID senderUUID, String playerNameToAdd) {
        if(senderUUID == null || playerNameToAdd == null) {
            return;
        }

        CloudPlayer senderPlayer = this.getCloudPlayerById(senderUUID);
        if(senderPlayer == null) {
            return;
        }



    }
}
