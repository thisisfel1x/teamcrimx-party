package net.teamcrimx.party.cloud.listener.player;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerDisconnectEvent;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.teamcrimx.party.api.PartyConstants;
import net.teamcrimx.party.api.SimpleParty;
import net.teamcrimx.party.cloud.PartyModule;

import java.util.Random;
import java.util.UUID;

public class ProxyDisconnectListener {

    private final PartyModule partyModule;

    public ProxyDisconnectListener(PartyModule partyModule) {
        this.partyModule = partyModule;
    }

    @EventListener
    public void on(BridgeProxyPlayerDisconnectEvent event) {
        // TODO: check for party. move methodes. if player isn't owner of party just leave, else promote a random player
        CloudPlayer cloudPlayer = event.cloudPlayer();

        if(cloudPlayer.properties().contains(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY)
                && cloudPlayer.properties().getBoolean(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY)) {

            UUID partyUUID = cloudPlayer.properties().get(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY,
                    UUID.class, null);
            if(partyUUID == null) {
                return; // TODO: fehler
            }

            SimpleParty simpleParty = this.partyModule.getPartiesTracker().activeParties().get(partyUUID);
            if(simpleParty == null) {
                return; // TODO: kp
            }

            simpleParty.partyMembers().remove(cloudPlayer.uniqueId());

            if(simpleParty.partyLeader() == cloudPlayer.uniqueId()) {
                if(simpleParty.partyMembers().size() > 0) {
                    UUID newLeader = simpleParty.partyMembers().get(new Random().nextInt(simpleParty.partyMembers().size()));
                    simpleParty.partyLeader(newLeader);
                    this.partyModule.getPartiesTracker().activeParties().put(partyUUID, simpleParty);
                    System.out.println(this.partyModule.getPartiesTracker().activeParties().get(partyUUID).toString());
                } else {
                    this.partyModule.getPartiesTracker().activeParties().remove(partyUUID);
                    System.out.println(this.partyModule.getPartiesTracker().activeParties().size());
                }
            }

            cloudPlayer.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
            cloudPlayer.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
            this.partyModule.playerManager().updateOnlinePlayer(cloudPlayer);

        }

    }

}
