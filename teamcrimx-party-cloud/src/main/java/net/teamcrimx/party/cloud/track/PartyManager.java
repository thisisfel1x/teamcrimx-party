package net.teamcrimx.party.cloud.track;

import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.teamcrimx.party.api.PartyConstants;
import net.teamcrimx.party.api.SimpleParty;
import net.teamcrimx.party.cloud.PartyModule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PartyManager {

    private final PartyModule partyModule;

    public PartyManager(PartyModule partyModule) {
        this.partyModule = partyModule;
    }

    public void createParty(@NotNull DataBuf content) {
        UUID playerUUID = content.readUniqueId();
        CloudPlayer cloudPlayer = this.partyModule.playerManager().onlinePlayer(playerUUID);
        if(cloudPlayer == null) {
            return;
        }

        UUID partyId = UUID.randomUUID();

        cloudPlayer.properties().append(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY, true)
                .append(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY, partyId);
        this.partyModule.playerManager().updateOnlinePlayer(cloudPlayer);

        List<UUID> partyPlayers = new ArrayList<>();
        partyPlayers.add(playerUUID);

        SimpleParty simpleParty = new SimpleParty(partyId, playerUUID, partyPlayers);

        this.partyModule.getPartiesTracker().activeParties().put(partyId, simpleParty);
        cloudPlayer.playerExecutor().sendChatMessage(Component.text("Deine Party wurde erstellt"));

    }

    public void leaveParty(DataBuf content) {
        UUID playerUUID = content.readUniqueId();
        CloudPlayer cloudPlayer = this.partyModule.playerManager().onlinePlayer(playerUUID);
        if(cloudPlayer == null) {
            return;
        }

        UUID partyId = cloudPlayer.properties().get(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY, UUID.class);
        SimpleParty simpleParty = this.partyModule.getPartiesTracker().activeParties().get(partyId);

        simpleParty.partyMembers().remove(playerUUID);

        if(playerUUID == simpleParty.partyLeader()) {

        }

    }
}
