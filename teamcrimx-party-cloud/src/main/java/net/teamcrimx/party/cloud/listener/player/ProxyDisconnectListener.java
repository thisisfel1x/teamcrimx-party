package net.teamcrimx.party.cloud.listener.player;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerDisconnectEvent;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.teamcrimx.party.cloud.PartyModule;

public class ProxyDisconnectListener {

    private final PartyModule partyModule;

    public ProxyDisconnectListener(PartyModule partyModule) {
        this.partyModule = partyModule;
    }

    @EventListener
    public void on(BridgeProxyPlayerDisconnectEvent event) {
        System.out.println("Disconnect > " + event.cloudPlayer().playerExecutor());
        this.partyModule.getPartyManager().removeFromPartyIfIn(event.cloudPlayer().uniqueId());

    }

}
