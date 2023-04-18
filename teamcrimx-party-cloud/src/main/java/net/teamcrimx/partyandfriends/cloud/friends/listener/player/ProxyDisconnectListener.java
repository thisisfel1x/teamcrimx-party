package net.teamcrimx.partyandfriends.cloud.friends.listener.player;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerDisconnectEvent;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;

public class ProxyDisconnectListener {

    private final PartyAndFriendsModule partyAndFriendsModule;

    public ProxyDisconnectListener(PartyAndFriendsModule partyAndFriendsModule) {
        this.partyAndFriendsModule = partyAndFriendsModule;
    }

    @EventListener
    public void on(BridgeProxyPlayerDisconnectEvent event) {
        this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(event.cloudPlayer().uniqueId()).update(false);
    }
}
