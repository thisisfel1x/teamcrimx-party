package net.teamcrimx.partyandfriends.cloud.friends.listener;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import net.teamcrimx.partyandfriends.api.friends.FriendConstants;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;

public class ChannelFriendMessageReceiveListener {

    private final PartyAndFriendsModule partyAndFriendsModule;

    public ChannelFriendMessageReceiveListener(PartyAndFriendsModule partyAndFriendsModule) {
        this.partyAndFriendsModule = partyAndFriendsModule;
    }

    @EventListener
    public void on(ChannelMessageReceiveEvent event) {
        if(!event.channel().equalsIgnoreCase(FriendConstants.FRIEND_CHANNEL)) {
            return;
        }

        DataBuf content = event.content();

        switch (event.message().toLowerCase()) {
            case FriendConstants.FRIEND_ADD_MESSAGE -> this.partyAndFriendsModule.friendManager()
                    .addFriend(content.readUniqueId(), content.readString());
            case FriendConstants.FRIEND_ACCEPT_MESSAGE -> this.partyAndFriendsModule.friendManager()
                    .executeSingle(content.readUniqueId(), content.readString(), FriendConstants.FRIEND_ACCEPT_MESSAGE);
            case FriendConstants.FRIEND_ACCEPT_ALL_MESSAGE -> this.partyAndFriendsModule.friendManager()
                    .executeToAll(content.readUniqueId(), FriendConstants.FRIEND_ACCEPT_MESSAGE);
            case FriendConstants.FRIEND_DENY_MESSAGE -> this.partyAndFriendsModule.friendManager()
                    .executeSingle(content.readUniqueId(), content.readString(), FriendConstants.FRIEND_DENY_MESSAGE);
            case FriendConstants.FRIEND_DENY_ALL_MESSAGE -> this.partyAndFriendsModule.friendManager()
                    .executeToAll(content.readUniqueId(), FriendConstants.FRIEND_DENY_MESSAGE);
            case FriendConstants.FRIEND_REMOVE_MESSAGE -> this.partyAndFriendsModule.friendManager()
                    .executeSingle(content.readUniqueId(), content.readString(), FriendConstants.FRIEND_REMOVE_MESSAGE);
            case FriendConstants.FRIEND_REMOVE_ALL_MESSAGE -> this.partyAndFriendsModule.friendManager()
                    .executeToAll(content.readUniqueId(), FriendConstants.FRIEND_REMOVE_MESSAGE);
            case FriendConstants.FRIEND_LIST_MESSAGE -> this.partyAndFriendsModule.friendManager().listFriends(content.readUniqueId());
            case FriendConstants.FRIEND_LIST_REQUESTS_MESSAGE -> this.partyAndFriendsModule.friendManager().listFriendRequests(content.readUniqueId());
            case FriendConstants.FRIEND_JUMP_MESSAGE -> this.partyAndFriendsModule.friendManager().jumpToPlayer(content.readUniqueId(), content.readString());
        }

    }

}
