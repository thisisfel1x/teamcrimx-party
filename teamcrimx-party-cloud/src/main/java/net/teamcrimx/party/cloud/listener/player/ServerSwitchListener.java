package net.teamcrimx.party.cloud.listener.player;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.service.ServiceEnvironmentType;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerServerSwitchEvent;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.teamcrimx.party.api.SimpleParty;
import net.teamcrimx.party.cloud.PartyModule;

import java.util.Objects;
import java.util.UUID;

public class ServerSwitchListener {

    private final PartyModule partyModule;

    public ServerSwitchListener(PartyModule partyModule) {
        this.partyModule = partyModule;
    }

    @EventListener
    public void on(BridgeProxyPlayerServerSwitchEvent event) {
        CloudPlayer cloudPlayer = event.cloudPlayer();

        System.out.println(event.target().environment());

        if(Objects.equals(event.target().taskName(), "Proxy") || Objects.equals(event.target().serverName(), "Node")) { // TODO: hardcoded
            System.out.println("no");
            return;
        }

        if(!this.partyModule.getPartyManager().isInParty(cloudPlayer)) {
            System.out.println("ab");
            return;
        }

        SimpleParty simpleParty = this.partyModule.getPartyManager().getPartyByCloudPlayer(cloudPlayer);
        if(simpleParty == null || !cloudPlayer.uniqueId().toString().equalsIgnoreCase(simpleParty.partyLeader().toString())) { // Perm check
            System.out.println("cd");
            return;
        }

        for (UUID partyMember : simpleParty.partyMembers()) {
            if(partyMember == cloudPlayer.uniqueId()) {
                System.out.println("ef");
                continue;
            }
            try {
                this.partyModule.getPartyManager().getCloudPlayerById(partyMember)
                        .playerExecutor().connect(event.target().serverName()); // TODO: check if his server is ingame
            } catch (NullPointerException ignored) {
                // remove player from party
                System.out.println(ignored.getMessage());
                // TODO: send message that x players couldnt join
            }
        }
    }

}
