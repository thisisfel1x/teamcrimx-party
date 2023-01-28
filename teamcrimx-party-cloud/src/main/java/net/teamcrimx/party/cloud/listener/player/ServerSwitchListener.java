package net.teamcrimx.party.cloud.listener.player;

import eu.cloudnetservice.driver.service.ServiceEnvironmentType;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerServerSwitchEvent;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.teamcrimx.party.api.SimpleParty;
import net.teamcrimx.party.cloud.PartyModule;

import java.util.UUID;

public class ServerSwitchListener {

    private final PartyModule partyModule;

    public ServerSwitchListener(PartyModule partyModule) {
        this.partyModule = partyModule;
    }

    public void on(BridgeProxyPlayerServerSwitchEvent event) {
        CloudPlayer cloudPlayer = event.cloudPlayer();

        if(event.target().environment() != ServiceEnvironmentType.MINECRAFT_SERVER) {
            return;
        }

        if(!this.partyModule.getPartyManager().isInParty(cloudPlayer)) {
            return;
        }

        SimpleParty simpleParty = this.partyModule.getPartyManager().getPartyByCloudPlayer(cloudPlayer);
        if(simpleParty == null || simpleParty.partyLeader() != cloudPlayer.uniqueId()) { // Perm check
            return;
        }

        for (UUID partyMember : simpleParty.partyMembers()) {
            if(partyMember == cloudPlayer.uniqueId()) {
                continue;
            }
            try {
                this.partyModule.getPartyManager().getCloudPlayerById(partyMember)
                        .playerExecutor().connect(event.target().serverName()); // TODO: check if his server is ingame
            } catch (NullPointerException ignored) {
                // remove player from party
                // TODO: send message that x players couldnt join
            }
        }
    }

}
