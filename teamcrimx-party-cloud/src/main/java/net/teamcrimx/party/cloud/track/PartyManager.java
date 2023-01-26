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

    public void parsePlayerNameAndExecute(DataBuf content, String partyConstant) {
        String playerName = content.readString();
        CloudPlayer cloudPlayer = this.partyModule.playerManager().firstOnlinePlayer(playerName);

        switch (partyConstant) {
            case PartyConstants.PARTY_KICK_MESSAGE -> this.kick(cloudPlayer, content.readUniqueId(), false);
            case PartyConstants.PARTY_INVITE_MESSAGE -> this.invite(cloudPlayer, content.readUniqueId());
            case PartyConstants.PARTY_PROMOTE_MESSAGE -> this.promotePlayer(cloudPlayer.uniqueId(), content.readUniqueId());
            case PartyConstants.PARTY_JOIN_MESSAGE -> this.join(cloudPlayer, content.readUniqueId());
        }
    }

    public void createParty(@NotNull DataBuf content) {
        UUID playerId = content.readUniqueId();
        CloudPlayer cloudPlayer = this.getCloudPlayerById(playerId);
        if(cloudPlayer == null) {
            return; // TODO: Fehler bitte asap beheben
        }

        if(this.isInParty(cloudPlayer)) {
            cloudPlayer.playerExecutor().sendChatMessage(Component.text("Du bist bereits in einer Party"));
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
        cloudPlayer.playerExecutor().sendChatMessage(Component.text("Deine Party wurde erstellt " + simpleParty));

    }

    private void invite(CloudPlayer cloudPlayerToInvite, UUID senderId) {
        // Step 1: basic checks
        SimpleParty simpleParty = this.getPartyByPlayerId(senderId);
        CloudPlayer sender = this.getCloudPlayerById(senderId);
        if (simpleParty == null || sender == null) {
            // TODO: kp
            return;
        }

        // perm check
        if (!senderId.toString().equalsIgnoreCase(simpleParty.partyLeader().toString())) {
            sender.playerExecutor().sendChatMessage(Component.text("du bist nicht der party leader"));
            return; // TODO: send message kein admin
        }

        // wtf spieler will sich selber einladen
        if (senderId == cloudPlayerToInvite.uniqueId()) {
            sender.playerExecutor().sendChatMessage(Component.text("du kannst dich nicht selbst einladen"));
            return;
        }

        if (simpleParty.partyMembers().contains(cloudPlayerToInvite.uniqueId())) {
            sender.playerExecutor().sendChatMessage(Component.text("der spieler ist bereits in deiner party"));
            return;
        }

        // Step 2 - check if player is already in a party
        if (this.isInParty(cloudPlayerToInvite)) {
            System.out.println("in other party");
            sender.playerExecutor().sendChatMessage(Component.text("der spieler ist bereits in einer anderen party"));
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

        sender.playerExecutor().sendChatMessage(Component.text("der spieler " + cloudPlayerToInvite.name()
                + " wurde erfolgreich eingeladen"));
    }

    private void join(CloudPlayer cloudPlayerToJoin, UUID invitedPlayerId) {
        CloudPlayer invitedCloudPlayer = this.getCloudPlayerById(invitedPlayerId);
        if(invitedCloudPlayer == null) {
            return;
        }

        if(this.isInParty(invitedCloudPlayer)) {
            invitedCloudPlayer.playerExecutor()
                    .sendChatMessage(Component.text("Du bist bereits in einer Party und kannst somit keiner anderen Party beitreten"));
            return; // prevent of joining a party while being in a party
        }

        SimpleParty simpleParty = this.getPartyByCloudPlayer(cloudPlayerToJoin);
        if(simpleParty == null) {
            invitedCloudPlayer.playerExecutor().sendChatMessage(Component.text("die party existiert nicht mehr"));
            return;
        }

        if(simpleParty.partyMembers().contains(invitedPlayerId)) {
            invitedCloudPlayer.playerExecutor().sendChatMessage(Component.text("du bist bereits in der party. warum hast du noch ein einladung"));
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
        cloudPlayerToJoin.playerExecutor()
                .sendChatMessage(Component.text("der spieler " + invitedCloudPlayer.name() + " ist deiner party beigetreten"));

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

            if(!simpleParty.partyMembers().contains(cloudPlayer.uniqueId())) {
                System.out.println("debug a");
                return;
            }

            simpleParty.partyMembers().remove(cloudPlayer.uniqueId());

            if(simpleParty.partyLeader() == cloudPlayer.uniqueId()) {
                if(simpleParty.partyMembers().size() > 0) {
                    this.promotePlayer( null, cloudPlayer.uniqueId()); // promote random player because leader has disconnected
                    System.out.println(this.partyModule.getPartiesTracker().activeParties().get(simpleParty.partyId()).toString());
                } else {
                    this.partyModule.getPartiesTracker().activeParties().remove(simpleParty.partyId());
                    System.out.println(this.partyModule.getPartiesTracker().activeParties().size());
                }
            }

            cloudPlayer.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
            cloudPlayer.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
            this.partyModule.playerManager().updateOnlinePlayer(cloudPlayer);

            cloudPlayer.playerExecutor().sendChatMessage(Component.text("du hast die party verlassen"));
    }

    public void promotePlayer(@Nullable UUID playerToPromote, @Nullable UUID senderId) {
        SimpleParty simpleParty = this.getPartyByPlayerId(senderId);
        CloudPlayer senderPlayer = this.getCloudPlayerById(senderId);
        // if playerToPromote is null, pick a random player (e.g. caused by leader disconnect)
        if (simpleParty == null || senderPlayer == null) {
            return;
        }

        // perms check
        if (senderId != simpleParty.partyLeader()) {
            senderPlayer.playerExecutor().sendChatMessage(Component.text("du bist kein leader du trottel"));
            return; // TODO message
        }

        // leader kann sich nicht zum leader machen wtf
        if (senderId == playerToPromote) {
            senderPlayer.playerExecutor().sendChatMessage(Component.text("du kannst dich nicht selber zum leader machen wenn du es schon bist du hirni"));
            return;
        }

        UUID newLeader;
        if (playerToPromote != null) {
            if(!simpleParty.partyMembers().contains(playerToPromote)) {
                // TODO ja moin der spieler ist nicht in der party
            }
            newLeader = playerToPromote;
            return;
        } else {
            newLeader = simpleParty.partyMembers().get(new Random().nextInt(simpleParty.partyMembers().size()));
            this.getCloudPlayerById(newLeader).playerExecutor().sendChatMessage(Component.text("du bist nun neuer leader der party opfer"));
        }
        simpleParty.partyLeader(newLeader);
        this.partyModule.getPartiesTracker().activeParties().put(simpleParty.partyId(), simpleParty);
    }

    public void kick(CloudPlayer cloudPlayerToKick, UUID senderId, boolean force) { // TODO: hier klappt gar nix leck mich am arsch
        // Step 1: remove him from partyArrayList
        SimpleParty simpleParty = this.getPartyByPlayerId(senderId); // mistake, senderId is actual owner of party
        if(simpleParty == null) {
            // TODO: kp
            return;
        }

        // perm check
        if(senderId != simpleParty.partyLeader() && !force) {
            return; // TODO: send message kein admin
        }

        // wtf spieler will sich selber kicken
        if(senderId == cloudPlayerToKick.uniqueId() && !force) {
            return;
        }

        if(!simpleParty.partyMembers().contains(cloudPlayerToKick.uniqueId())) {
            return; // TODO der bruder ist nicht in der party also sende nachricht
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
            this.kick(this.getCloudPlayerById(partyMember), simpleParty.partyLeader(), true); // force for kicking leader as well
        }

        this.partyModule.getPartiesTracker().activeParties().remove(simpleParty.partyId());

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
