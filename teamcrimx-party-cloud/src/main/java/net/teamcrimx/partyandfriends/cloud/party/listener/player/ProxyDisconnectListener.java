package net.teamcrimx.partyandfriends.cloud.party.listener.player;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerDisconnectEvent;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerLoginEvent;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;

public class ProxyDisconnectListener {

    private final PartyAndFriendsModule partyAndFriendsModule;

    public ProxyDisconnectListener(PartyAndFriendsModule partyAndFriendsModule) {
        this.partyAndFriendsModule = partyAndFriendsModule;
    }

    @EventListener
    public void on(BridgeProxyPlayerDisconnectEvent event) {
        this.partyAndFriendsModule.getPartyManager().removeFromParty(event.cloudPlayer().uniqueId());
    }

    @EventListener
    public void on(BridgeProxyPlayerLoginEvent event) {
        this.partyAndFriendsModule.getPartyManager().delete(event.cloudPlayer().uniqueId()); // temporary
    }

}
