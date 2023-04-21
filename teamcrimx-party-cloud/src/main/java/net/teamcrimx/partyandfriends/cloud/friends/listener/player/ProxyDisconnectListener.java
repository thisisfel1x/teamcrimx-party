package net.teamcrimx.partyandfriends.cloud.friends.listener.player;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerDisconnectEvent;
import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;
import net.teamcrimx.partyandfriends.cloud.friends.manager.FriendManager;

public class ProxyDisconnectListener {

    private final PartyAndFriendsModule partyAndFriendsModule;

    public ProxyDisconnectListener(PartyAndFriendsModule partyAndFriendsModule) {
        this.partyAndFriendsModule = partyAndFriendsModule;
    }

    @EventListener
    public void on(BridgeProxyPlayerDisconnectEvent event) {
        SimpleFriend simpleFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(event.cloudPlayer().uniqueId());
        this.partyAndFriendsModule.friendManager().notifyFriends(simpleFriend.uuid(), FriendManager.NotifyType.OFFLINE);

        simpleFriend.update(false);

        this.partyAndFriendsModule.friendHolder().simpleFriendMap().remove(event.cloudPlayer().uniqueId());
    }
}
