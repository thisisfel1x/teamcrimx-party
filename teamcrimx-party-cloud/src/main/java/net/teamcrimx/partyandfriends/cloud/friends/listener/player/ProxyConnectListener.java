package net.teamcrimx.partyandfriends.cloud.friends.listener.player;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerLoginEvent;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;

public class ProxyConnectListener {

    private final PartyAndFriendsModule partyAndFriendsModule;

    public ProxyConnectListener(PartyAndFriendsModule partyAndFriendsModule) {
        this.partyAndFriendsModule = partyAndFriendsModule;
    }

    @EventListener
    public void on(BridgeProxyPlayerLoginEvent event) {

        // TODO : get friends or check for database

    }

}
