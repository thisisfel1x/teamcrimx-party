package net.teamcrimx.party.cloud.listener.player;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerDisconnectEvent;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerLoginEvent;
import net.teamcrimx.party.cloud.PartyModule;

public class ProxyDisconnectListener {

    private final PartyModule partyModule;

    public ProxyDisconnectListener(PartyModule partyModule) {
        this.partyModule = partyModule;
    }

    @EventListener
    public void on(BridgeProxyPlayerDisconnectEvent event) {
        this.partyModule.getPartyManager().removeFromParty(event.cloudPlayer().uniqueId());
    }

    @EventListener
    public void on(BridgeProxyPlayerLoginEvent event) {
        this.partyModule.getPartyManager().delete(event.cloudPlayer().uniqueId()); // temporary
    }

}
