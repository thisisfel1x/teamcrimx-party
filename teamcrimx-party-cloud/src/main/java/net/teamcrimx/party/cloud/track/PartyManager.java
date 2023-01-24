package net.teamcrimx.party.cloud.track;

import eu.cloudnetservice.common.document.gson.JsonDocument;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.teamcrimx.party.api.PartyConstants;
import net.teamcrimx.party.api.SimpleParty;
import net.teamcrimx.party.cloud.PartyModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

// TODO: perm fix
public class PartyManager {

    private final PartyModule partyModule;

    public PartyManager(PartyModule partyModule) {
        this.partyModule = partyModule;
    }

    public @Nullable CloudPlayer getCloudPlayerById(UUID playerId) {
        return this.partyModule.playerManager().onlinePlayer(playerId);
    }

    public @Nullable SimpleParty getPartyById(UUID partyId) { // großes problem: keine abfrage, ob der spieler aktuell admin ist oder nicht
        return this.partyModule.getPartiesTracker().activeParties().get(partyId);
    }

    public @Nullable SimpleParty getPartyByPlayerId(UUID playerId) {
        CloudPlayer cloudPlayer = this.getCloudPlayerById(playerId);
        if(cloudPlayer == null) {
            return null;
        }
        return getPartyByCloudPlayer(cloudPlayer);
    }

    public @Nullable SimpleParty getPartyByCloudPlayer(CloudPlayer cloudPlayer) {
        if(cloudPlayer.properties().contains(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY)
                && cloudPlayer.properties().getBoolean(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY)) {

            UUID partyUUID = cloudPlayer.properties().get(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY,
                    UUID.class);
            if (partyUUID == null) {
                return null; // TODO: fehler
            }

            return this.getPartyById(partyUUID);
        }
        return null;
    }

    public boolean isInParty(CloudPlayer cloudPlayer) {
        return cloudPlayer.properties().contains(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY)
                && cloudPlayer.properties().getBoolean(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY, false);
    }

    public boolean isPartyLeader(CloudPlayer cloudPlayer, SimpleParty simpleParty) {
        return cloudPlayer.uniqueId() == simpleParty.partyLeader();
    }

    public void createParty(@NotNull DataBuf content) {
        UUID playerId = content.readUniqueId();
        CloudPlayer cloudPlayer = this.getCloudPlayerById(playerId);
        if(cloudPlayer == null) {
            return; // TODO: Fehler
        }

        if(this.isInParty(cloudPlayer)) {
            return; // prevent of creating a party while being in a party
        }

        UUID partyId = UUID.randomUUID();

        cloudPlayer.properties().append(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY, true)
                .append(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY, partyId);
        this.partyModule.playerManager().updateOnlinePlayer(cloudPlayer);

        List<UUID> partyPlayers = new ArrayList<>();
        partyPlayers.add(playerId);

        SimpleParty simpleParty = new SimpleParty(partyId, playerId, partyPlayers);

        System.out.println(simpleParty.toString());

        this.partyModule.getPartiesTracker().activeParties().put(partyId, simpleParty);
        cloudPlayer.playerExecutor().sendChatMessage(Component.text("Deine Party wurde erstellt"));

    }

    public void leaveParty(DataBuf content) {
        UUID playerId = content.readUniqueId();
        CloudPlayer cloudPlayer = this.getCloudPlayerById(playerId);
        if(cloudPlayer == null) {
            return;
        }

        this.removeFromPartyIfIn(cloudPlayer);
    }

    public void removeFromPartyIfIn(@NotNull CloudPlayer cloudPlayer) {
            SimpleParty simpleParty = this.getPartyByCloudPlayer(cloudPlayer);
            if(simpleParty == null) {
                return; // TODO: kp
            }

            simpleParty.partyMembers().remove(cloudPlayer.uniqueId());

            if(simpleParty.partyLeader() == cloudPlayer.uniqueId()) {
                if(simpleParty.partyMembers().size() > 0) {
                    this.promotePlayer(simpleParty, null, null); // promote random player because leader has disconnected
                    System.out.println(this.partyModule.getPartiesTracker().activeParties().get(simpleParty.partyId()).toString());
                } else {
                    this.partyModule.getPartiesTracker().activeParties().remove(simpleParty.partyId());
                    System.out.println(this.partyModule.getPartiesTracker().activeParties().size());
                }
            }

            cloudPlayer.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
            cloudPlayer.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
            this.partyModule.playerManager().updateOnlinePlayer(cloudPlayer);
    }

    public void promotePlayer(@UnknownNullability SimpleParty simpleParty, @Nullable UUID playerToPromote, @Nullable UUID senderId) {
        // if playerToPromote is null, pick a random player (e.g. caused by leader disconnect)
        if(simpleParty == null) {
            return;
        }

        // perms check
        if(senderId != simpleParty.partyLeader()) {
            return; // TODO message
        }

        // leader kann sich nicht zum leader machen wtf
        if(senderId == playerToPromote) {
            return;
        }

        UUID newLeader;
        if(playerToPromote != null) {
            newLeader = playerToPromote;
        } else {
            newLeader = simpleParty.partyMembers().get(new Random().nextInt(simpleParty.partyMembers().size()));
        }
        simpleParty.partyLeader(newLeader);
        this.partyModule.getPartiesTracker().activeParties().put(simpleParty.partyId(), simpleParty);
    }

    public void parsePlayerNameThenPromote(DataBuf content) {
        String playerName = content.readString();

        CloudPlayer cloudPlayer = this.partyModule.playerManager().firstOnlinePlayer(playerName);
        if(cloudPlayer == null) {
            // TODO: send fallback message to player who tried to promote
        } else {
            this.promotePlayer(this.getPartyByPlayerId(cloudPlayer.uniqueId()),
                    cloudPlayer.uniqueId(), content.readUniqueId()); // TODO send message to him
        }
    }

    public void parsePlayerNameThenKick(DataBuf content) {
        String playerName = content.readString();

        CloudPlayer cloudPlayer = this.partyModule.playerManager().firstOnlinePlayer(playerName);

        if(cloudPlayer == null) {
            // TODO: send fallback message to player who tried to kick player x
        } else {
            this.kick(cloudPlayer, content.readUniqueId());
        }

    }

    public void kick(CloudPlayer cloudPlayerToKick, UUID senderId) {
        // Step 1: remove him from partyArrayList
        SimpleParty simpleParty = this.getPartyByCloudPlayer(cloudPlayerToKick);
        if(simpleParty == null) {
            // TODO: kp
            return;
        }

        // perm check
        if(senderId != simpleParty.partyLeader()) {
            return; // TODO: send message kein admin
        }

        // wtf spieler will sich selber kicken
        if(senderId == cloudPlayerToKick.uniqueId()) {
            return;
        }

        // update party object
        simpleParty.partyMembers().remove(cloudPlayerToKick.uniqueId());
        this.partyModule.getPartiesTracker().activeParties().put(simpleParty.partyId(), simpleParty);

        // Step 2: remove his properties and update him
        cloudPlayerToKick.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
        cloudPlayerToKick.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
        this.partyModule.playerManager().updateOnlinePlayer(cloudPlayerToKick);

        // Done, send him a message
        cloudPlayerToKick.playerExecutor().sendChatMessage(Component.text("du wurdest aus der party gekickt du hurensohn"));
    }

    public void closeParty(DataBuf content) {
        UUID playerId = content.readUniqueId();
        SimpleParty simpleParty = this.getPartyByPlayerId(playerId);

        if(simpleParty == null) {
            return; // TODO kp
        }

        // permission check
        if(playerId != simpleParty.partyLeader()) {
            // TODO: send message was weiß ich
            return;
        }

        // Loop through all players and let them quit
        for (UUID partyMember : simpleParty.partyMembers()) {
            this.kick(this.getCloudPlayerById(partyMember), simpleParty.partyLeader()); // use partyLeader as arg for skipping perm check
        }

        this.partyModule.getPartiesTracker().activeParties().remove(simpleParty.partyId());

    }

    public void parsePlayerNameThenInvite(DataBuf content) {
        String playerName = content.readString();
        CloudPlayer cloudPlayer = this.partyModule.playerManager().firstOnlinePlayer(playerName);

        if(cloudPlayer == null) {
            System.out.println("null");
            // TODO: send fallback message to player who tried to invite player x
        } else {
            this.invite(cloudPlayer, content.readUniqueId());
        }
    }

    private void invite(CloudPlayer cloudPlayerToInvite, UUID senderId) {
        // Step 1: basic checks
        SimpleParty simpleParty = this.getPartyByPlayerId(senderId);
        if(simpleParty == null) {
            System.out.println("a");
            // TODO: kp
            return;
        }

        // perm check
        if(!senderId.toString().equalsIgnoreCase(simpleParty.partyLeader().toString())) {
            System.out.println(senderId);
            System.out.println(simpleParty.partyLeader());
            System.out.println("b");
            return; // TODO: send message kein admin
        }

        // wtf spieler will sich selber einladen
        if(senderId == cloudPlayerToInvite.uniqueId()) {
            System.out.println("c");
            return;
        }

        if(simpleParty.partyMembers().contains(cloudPlayerToInvite.uniqueId())) {
            System.out.println("d");
            return;
        }

        // Step 2 - check if player is already in a party
        if(this.isInParty(cloudPlayerToInvite)) {
            System.out.println("in party");
            return; // spieler hat bereits eine party
        }

        // Step 3 - add document property with id and expiration
        // TODO: implement check for type safety
        JsonDocument invites = cloudPlayerToInvite.properties().getDocument("invites");

        invites.append(simpleParty.partyId().toString(), System.currentTimeMillis() + (5 * 60 * 1000)); // expiration after 5 min

        cloudPlayerToInvite.properties().append(PartyConstants.PARTY_INVITATIONS_DOCUMENT_PROPERTY, invites);
        this.partyModule.playerManager().updateOnlinePlayer(cloudPlayerToInvite);

        cloudPlayerToInvite.playerExecutor().sendChatMessage(Component.text("du hast einen invite von "
                + senderId.toString() + " erhalten, rein da"));

    }

    public void parsePlayerNameThenJoinParty(DataBuf content) {
        String playerName = content.readString();
        CloudPlayer cloudPlayer = this.partyModule.playerManager().firstOnlinePlayer(playerName);

        if(cloudPlayer == null) {
            // TODO: send fallback message to player who tried to join player x
        } else {
            this.join(cloudPlayer, content.readUniqueId());
        }
    }

    private void join(CloudPlayer cloudPlayerToJoin, UUID invitedPlayerId) {
        CloudPlayer invitedCloudPlayer = this.getCloudPlayerById(invitedPlayerId);
        if(invitedCloudPlayer == null) {
            return;
        }

        if(this.isInParty(invitedCloudPlayer)) {
            return; // prevent of joining a party while being in a party
        }

        SimpleParty simpleParty = this.getPartyByCloudPlayer(cloudPlayerToJoin);
        if(simpleParty == null) {
            invitedCloudPlayer.playerExecutor().sendChatMessage(Component.text("die party existiert nicht mehr"));
            return;
        }

       JsonDocument invites = invitedCloudPlayer.properties().getDocument(PartyConstants.PARTY_INVITATIONS_DOCUMENT_PROPERTY);

        // Check expiration
        if(!invites.contains(simpleParty.partyId().toString())) {
            invitedCloudPlayer.playerExecutor().sendChatMessage(Component.text("du hast keine einladung erhalten"));
            return;
        }

        if(System.currentTimeMillis() > invites.getLong(simpleParty.partyId().toString())) {
            invitedCloudPlayer.playerExecutor().sendChatMessage(Component.text("die einladung ist abgelaufen"));
        }

        // add him
        simpleParty.partyMembers().add(invitedCloudPlayer.uniqueId());
        this.partyModule.getPartiesTracker().activeParties().put(simpleParty.partyId(), simpleParty);

        invitedCloudPlayer.properties().append(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY, true)
                .append(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY, simpleParty.partyId())
                .remove(PartyConstants.PARTY_INVITATIONS_DOCUMENT_PROPERTY);
        this.partyModule.playerManager().updateOnlinePlayer(invitedCloudPlayer);

        invitedCloudPlayer.playerExecutor().sendChatMessage(Component.text("du bist der party beigetreten"));

    }

    public void delete(DataBuf content) {
        CloudPlayer cloudPlayer = getCloudPlayerById(content.readUniqueId());
        if(cloudPlayer == null) {
            return;
        }

        cloudPlayer.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
        cloudPlayer.properties().remove(PartyConstants.PARTY_INVITATIONS_DOCUMENT_PROPERTY);
        cloudPlayer.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
        this.partyModule.playerManager().updateOnlinePlayer(cloudPlayer);
    }
}
