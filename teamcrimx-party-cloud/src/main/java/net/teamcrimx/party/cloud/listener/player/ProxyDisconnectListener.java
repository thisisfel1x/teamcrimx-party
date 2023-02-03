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
        CloudPlayer cloudPlayer = event.cloudPlayer();

        // Partysystem: check and remove player if he is in an active party
        System.out.println(cloudPlayer);
        this.partyModule.getPartyManager().removeFromPartyIfIn(cloudPlayer);

    }

}
