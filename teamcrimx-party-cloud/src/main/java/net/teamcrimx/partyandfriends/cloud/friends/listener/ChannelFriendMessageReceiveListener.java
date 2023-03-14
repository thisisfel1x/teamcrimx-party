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
                    .acceptFriend(content.readUniqueId(), content.readString());
        }

    }

}
