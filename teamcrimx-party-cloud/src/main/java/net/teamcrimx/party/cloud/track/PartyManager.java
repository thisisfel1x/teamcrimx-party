package net.teamcrimx.party.cloud.track;

import eu.cloudnetservice.common.document.gson.JsonDocument;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudOfflinePlayer;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.teamcrimx.party.api.constants.ChatConstants;
import net.teamcrimx.party.api.party.PartyConstants;
import net.teamcrimx.party.api.party.SimpleParty;
import net.teamcrimx.party.cloud.PartyModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// TODO: perm fix
public class PartyManager {

    private final PartyModule partyModule;

    private final Component partyPrefix = Component.text("", NamedTextColor.GRAY)
            .append(ChatConstants.partyPrefix); // wtf

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
        if (cloudPlayer == null) {
            return null;
        }
        return getPartyByCloudPlayer(cloudPlayer);
    }

    public @Nullable SimpleParty getPartyByOfflinePlayerId(UUID playerId) {
        CloudOfflinePlayer cloudPlayer = this.partyModule.playerManager().offlinePlayer(playerId);
        if (cloudPlayer == null) {
            return null;
        }
        return getPartyByCloudPlayer(cloudPlayer);
    }

    public @Nullable SimpleParty getPartyByCloudPlayer(CloudOfflinePlayer cloudPlayer) {
        if (cloudPlayer.properties().contains(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY)
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

        if(cloudPlayer == null) {
            return;
        }

        switch (partyConstant) {
            case PartyConstants.PARTY_KICK_MESSAGE -> this.kick(cloudPlayer, content.readUniqueId(), false);
            case PartyConstants.PARTY_INVITE_MESSAGE -> this.invite(cloudPlayer, content.readUniqueId());
            case PartyConstants.PARTY_PROMOTE_MESSAGE ->
                    this.promotePlayer(cloudPlayer.uniqueId(), content.readUniqueId());
            case PartyConstants.PARTY_JOIN_MESSAGE -> this.join(cloudPlayer, content.readUniqueId());
        }
    }

    public void createParty(@NotNull DataBuf content) {
        UUID playerId = content.readUniqueId();
        CloudPlayer cloudPlayer = this.getCloudPlayerById(playerId);
        if (cloudPlayer == null) {
            return; // TODO: Fehler bitte asap beheben
        }

        if (this.isInParty(cloudPlayer)) {
            cloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist bereits in einer anderen Party")));
            return; // prevent of creating a party while being in a party
        }

        UUID partyId = UUID.randomUUID();

        cloudPlayer.properties().append(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY, true)
                .append(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY, partyId);
        this.partyModule.playerManager().updateOnlinePlayer(cloudPlayer);

        List<UUID> partyPlayers = new ArrayList<>();
        partyPlayers.add(playerId);

        SimpleParty simpleParty = new SimpleParty(partyId, playerId, partyPlayers, System.currentTimeMillis());

        System.out.println(simpleParty);

        this.partyModule.getPartiesTracker().activeParties().put(partyId, simpleParty);
        cloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Deine Party wurde erstellt")));
        cloudPlayer.playerExecutor().sendChatMessage(Component.text("DEBUG: partyId - " + simpleParty.partyId()));

    }

    private void invite(@NotNull CloudPlayer cloudPlayerToInvite, @Nullable UUID senderId) {
        // Step 1: basic checks
        CloudPlayer sender = this.getCloudPlayerById(senderId);
        if(sender == null) {
            return;
        }
        SimpleParty simpleParty = this.getPartyByPlayerId(senderId);
        if (simpleParty == null) {
            // TODO: kp
            sender.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist aktuell in keiner Party")));
            return;
        }

        // perm check
        if (!senderId.toString().equalsIgnoreCase(simpleParty.partyLeader().toString())) {
            sender.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist nicht der Party Leader")));
            return; // TODO: send message kein admin
        }

        // wtf spieler will sich selber einladen
        if (senderId.toString().equalsIgnoreCase(cloudPlayerToInvite.uniqueId().toString())) {
            sender.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du kannst dich nicht selber einladen")));
            return;
        }

        if (simpleParty.partyMembers().contains(cloudPlayerToInvite.uniqueId())) {
            sender.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Der Spieler ist bereits in deiner Party")));
            return;
        }

        // Step 2 - check if player is already in a party
        if (this.isInParty(cloudPlayerToInvite)) {
            sender.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Der Spieler ist bereits in einer anderen Party")));
            return; // spieler hat bereits eine party
        }

        // Step 3 - add document property with id and expiration
        // TODO: implement check for type safety
        JsonDocument invites = cloudPlayerToInvite.properties().getDocument("invites");

        invites.append(simpleParty.partyId().toString(), System.currentTimeMillis() + (5 * 60 * 1000)); // expiration after 5 min

        cloudPlayerToInvite.properties().append(PartyConstants.PARTY_INVITATIONS_DOCUMENT_PROPERTY, invites);
        this.partyModule.playerManager().updateOnlinePlayer(cloudPlayerToInvite);

        String senderName = "unknown";
        CloudPlayer cloudPlayer = this.getCloudPlayerById(senderId);
        if(cloudPlayer != null) {
            senderName = cloudPlayer.name();
        }

        cloudPlayerToInvite.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Du hast einen Invite zu der Party von " + senderName + " erhalten")));

        sender.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Der Spieler " + cloudPlayerToInvite.name() + " wurde erfolgreich eingeladen")));
    }

    private void join(@NotNull CloudPlayer cloudPlayerToJoin, @NotNull UUID invitedPlayerId) {
        CloudPlayer invitedCloudPlayer = this.getCloudPlayerById(invitedPlayerId);
        if (invitedCloudPlayer == null) {
            return;
        }

        if (this.isInParty(invitedCloudPlayer)) {
            invitedCloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist bereits in einer Party und kannst somit keiner anderen Party beitreten")));
            return; // prevent of joining a party while being in a party
        }

        SimpleParty simpleParty = this.getPartyByCloudPlayer(cloudPlayerToJoin);
        if (simpleParty == null) {
            invitedCloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Die Party existiert nicht mehr")));
            return;
        }

        if (simpleParty.partyMembers().contains(invitedPlayerId)) {
            invitedCloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist bereits in der Party")));
            return;
        }

        JsonDocument invites = invitedCloudPlayer.properties().getDocument(PartyConstants.PARTY_INVITATIONS_DOCUMENT_PROPERTY);

        // Check expiration
        if (!invites.contains(simpleParty.partyId().toString())) {
            invitedCloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du hast keine Einladung erhalten")));
            return;
        }

        if (System.currentTimeMillis() > invites.getLong(simpleParty.partyId().toString())) {
            invitedCloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Die Einladung ist abgelaufen")));
            // TODO: remove invite
        }

        // add him
        simpleParty.partyMembers().add(invitedCloudPlayer.uniqueId());
        this.partyModule.getPartiesTracker().activeParties().put(simpleParty.partyId(), simpleParty);

        invitedCloudPlayer.properties().append(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY, true)
                .append(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY, simpleParty.partyId())
                .remove(PartyConstants.PARTY_INVITATIONS_DOCUMENT_PROPERTY);
        this.partyModule.playerManager().updateOnlinePlayer(invitedCloudPlayer);

        invitedCloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Du bist der Party beigetreten")));
        cloudPlayerToJoin.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Der Spieler " + invitedCloudPlayer.name() + " ist deiner Party beigetreten")));

    }

    public void leaveParty(@NotNull DataBuf content) {
        UUID playerId = content.readUniqueId();
        this.removeFromPartyIfIn(playerId);
    }

    public void removeFromPartyIfIn(@NotNull UUID playerId) {
        CloudPlayer possibleOnlinePlayer = this.getCloudPlayerById(playerId); // might be null because he is offline
        SimpleParty simpleParty = this.getPartyByOfflinePlayerId(playerId); // <--- FEHLER

        if (simpleParty == null) {
            System.out.println(2);
            if(possibleOnlinePlayer  != null) {
                possibleOnlinePlayer.playerExecutor().sendChatMessage(this.partyPrefix
                        .append(Component.text("Du bist aktuell in keiner Party")));
            }
            return; // TODO: kp
        }

        System.out.println("Aus der Party will er raus: " + simpleParty.toString());

        if (!simpleParty.partyMembers().contains(playerId)) {
            if(possibleOnlinePlayer != null) {
                possibleOnlinePlayer.playerExecutor().sendChatMessage(this.partyPrefix
                        .append(Component.text("Irgendwie ist ein interner Fehler aufgetreten. Sorry!")));
            }
            System.out.println(3);
            return;
        } // TODO: remove doc

        simpleParty.partyMembers().remove(playerId);
        this.partyModule.getPartiesTracker().activeParties().put(simpleParty.partyId(), simpleParty);
        System.out.println(simpleParty.partyMembers().size());

        if (playerId.toString().equalsIgnoreCase(simpleParty.partyLeader().toString())) {
            if (simpleParty.partyMembers().size() > 0) {
                System.out.println(simpleParty);
                this.promotePlayer(null, playerId); // promote random player because leader has disconnected
                System.out.println(this.partyModule.getPartiesTracker().activeParties().get(simpleParty.partyId()).toString());
            } else {
                this.partyModule.getPartiesTracker().activeParties().remove(simpleParty.partyId());
                System.out.println(this.partyModule.getPartiesTracker().activeParties().size());
            }
        }

        CloudOfflinePlayer cloudOfflinePlayer = this.partyModule.playerManager().offlinePlayer(playerId);
        if(cloudOfflinePlayer == null) {
            return;
        }

        cloudOfflinePlayer.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
        cloudOfflinePlayer.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
        this.partyModule.playerManager().updateOfflinePlayer(cloudOfflinePlayer);

        if(possibleOnlinePlayer != null) {
            possibleOnlinePlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du hast die Party velassen")));
        }
    }

    public void promotePlayer(@Nullable UUID playerToPromote, @NotNull UUID senderId) {
        SimpleParty simpleParty = this.getPartyByOfflinePlayerId(senderId);
        CloudPlayer possibleOnlyPlayer = this.getCloudPlayerById(senderId);
        // if playerToPromote is null, pick a random player (e.g. caused by leader disconnect)
        if (simpleParty == null) {
            return;
        }

        // perms check
        if (!senderId.toString().equalsIgnoreCase(simpleParty.partyLeader().toString())) {
            if (possibleOnlyPlayer != null) {
                possibleOnlyPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                        .append(Component.text("Du bist kein Leader du Trottel")));
            }
            return; // TODO message
        }

        // leader kann sich nicht zum leader machen wtf
        if (playerToPromote != null && senderId.toString().equalsIgnoreCase(playerToPromote.toString())) {
            if (possibleOnlyPlayer != null) {
                possibleOnlyPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                        .append(Component.text("Du kannst dich nicht selber zum Leader machen du Hirni")));
            }
            return;
        }

        UUID newLeader;
        if (playerToPromote != null) {
            System.out.println("not null check false");
            if (!simpleParty.partyMembers().contains(playerToPromote)) {
                System.out.println("nicht in party");
                // TODO ja moin der spieler ist nicht in der party
                return;
            }
            newLeader = playerToPromote;
        } else {
            System.out.println(1);
            newLeader = simpleParty.partyMembers().get(new Random().nextInt(simpleParty.partyMembers().size()));
        }

        CloudPlayer leaderPlayer = this.getCloudPlayerById(newLeader);
        if(leaderPlayer != null) {
            leaderPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist nun der neue Leader der Party opfer")));
        }

        simpleParty.partyLeader(newLeader);
        this.partyModule.getPartiesTracker().activeParties().put(simpleParty.partyId(), simpleParty);
    }

    public void kick(@NotNull CloudPlayer cloudPlayerToKick, @NotNull UUID senderId, boolean force) { // TODO: hier klappt gar nix leck mich am arsch
        // Step 1: remove him from partyArrayList
        CloudPlayer senderPlayer = this.getCloudPlayerById(senderId);
        if(senderPlayer == null) {
            return;
        }

        SimpleParty simpleParty = this.getPartyByPlayerId(senderId); // mistake, senderId is actual owner of party
        if (simpleParty == null) {
            // TODO: kp
            return;
        }

        // perm check
        if (!senderId.toString().equalsIgnoreCase(simpleParty.partyLeader().toString()) && !force) {
            senderPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist kein Party Leader")));
            return; // TODO: send message kein admin
        }

        // wtf spieler will sich selber kicken
        if (Objects.equals(senderId.toString(), cloudPlayerToKick.uniqueId().toString()) && !force) {
            senderPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du kannst dich nicht selber kicken")));
            return;
        }

        if (!simpleParty.partyMembers().contains(cloudPlayerToKick.uniqueId())) {
            senderPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Der angegebene Spieler ist nicht in deiner Party")));
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
        cloudPlayerToKick.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Du wurdest aus der Party von " + senderPlayer.name() + " gekickt")));

        senderPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Du hast " + cloudPlayerToKick.name() + " aus der Party gekickt")));
    }

    public void closeParty(@NotNull DataBuf content) {
        UUID playerId = content.readUniqueId();
        SimpleParty simpleParty = this.getPartyByPlayerId(playerId);

        if (simpleParty == null) {
            return; // TODO kp
        }

        // permission check
        if (!playerId.toString().equalsIgnoreCase(simpleParty.partyLeader().toString())) {
            // TODO: send message was weiß ich
            return;
        }

        // Loop through all players and let them quit
        for (UUID partyMember : simpleParty.partyMembers()) {
            try {
                this.kick(this.getCloudPlayerById(partyMember), simpleParty.partyLeader(), true); // force for kicking leader as well
            } catch (Exception ignored) {
                // TODO: haha richtig witzig
            }
        }

        this.partyModule.getPartiesTracker().activeParties().remove(simpleParty.partyId());

    }

    public void delete(@NotNull DataBuf content) {
        CloudPlayer cloudPlayer = getCloudPlayerById(content.readUniqueId());
        if (cloudPlayer == null) {
            return;
        }

        cloudPlayer.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
        cloudPlayer.properties().remove(PartyConstants.PARTY_INVITATIONS_DOCUMENT_PROPERTY);
        cloudPlayer.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
        this.partyModule.playerManager().updateOnlinePlayer(cloudPlayer);
    }

    public void sendPartyListMessage(@NotNull DataBuf content) {
        CloudPlayer cloudPlayer = this.getCloudPlayerById(content.readUniqueId());

        if (cloudPlayer == null) {
            return;
        }

        SimpleParty simpleParty = this.getPartyByCloudPlayer(cloudPlayer);

        if (simpleParty == null) {
            cloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist in keiner Party oder deine Party wurde nicht gefunden")));
            return;
        }

        CloudPlayer leaderPlayer = this.getCloudPlayerById(simpleParty.partyLeader());
        Component leader = this.partyPrefix.append(Component.text("Leader: ein Fehler ist aufgetreten", NamedTextColor.RED));
        Component members = this.partyPrefix.append(Component.text("Mitglieder: ein Fehler ist aufgetreten", NamedTextColor.RED));
        if(leaderPlayer != null) {
            leader = this.partyPrefix.append(Component.text("Partyleader: " + leaderPlayer.name()));
            List<UUID> partyMembers = simpleParty.partyMembers();
            partyMembers.remove(simpleParty.partyLeader());

            if(partyMembers.size() == 0) {
                members = this.partyPrefix.append(Component.text("Deine Party ist leer"));
            } else {
                List<String> partyMemberNames = partyMembers.stream().map(uuid -> this.getCloudPlayerById(uuid).name()).toList(); // TODO: null safety
                members = this.partyPrefix.append(Component.text("Mitglieder: " + String.join(", ", partyMemberNames)));
            }
        }

        cloudPlayer.playerExecutor().sendChatMessage(leader.append(Component.newline().append(members)));
    }

    public void chat(@NotNull DataBuf content) {
        UUID senderId = content.readUniqueId();
        String message = content.readString();

        System.out.println(message);

        CloudPlayer cloudPlayer = this.getCloudPlayerById(senderId);
        if(cloudPlayer == null) {
            return;
        }

        if(!this.isInParty(cloudPlayer)) {
            cloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist in keiner Party oder deine Party wurde nicht gefunden")));
            return;
        }

        // Try to send every player a message
        SimpleParty simpleParty = this.getPartyByCloudPlayer(cloudPlayer);
        if(simpleParty == null) {
            return; // sollte eigentlich nicht passieren, siehe check oben
        }

        for (UUID partyMember : simpleParty.partyMembers()) {
            CloudPlayer partyMemberPlayer = this.getCloudPlayerById(partyMember);
            if(partyMemberPlayer == null) {
                continue;
            }

            partyMemberPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text(cloudPlayer.name(), NamedTextColor.GREEN))
                    .append(Component.text(" > ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(message, NamedTextColor.WHITE)));
        }

    }
}
