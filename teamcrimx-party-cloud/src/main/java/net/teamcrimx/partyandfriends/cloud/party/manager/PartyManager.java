package net.teamcrimx.partyandfriends.cloud.party.manager;

import eu.cloudnetservice.common.document.gson.JsonDocument;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudOfflinePlayer;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.teamcrimx.partyandfriends.api.constants.ChatConstants;
import net.teamcrimx.partyandfriends.api.party.PartyConstants;
import net.teamcrimx.partyandfriends.api.party.SimpleParty;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;
import net.teamcrimx.partyandfriends.cloud.SimpleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

// TODO: perm fix
public class PartyManager extends SimpleManager {

    private final PartyAndFriendsModule partyAndFriendsModule;

    private final Component partyPrefix = Component.text("", NamedTextColor.GRAY)
            .append(ChatConstants.partyPrefix); // wtf

    public PartyManager(PartyAndFriendsModule partyAndFriendsModule) {
        super(partyAndFriendsModule);
        this.partyAndFriendsModule = partyAndFriendsModule;

        this.prefix(this.partyPrefix);
    }

    public @Nullable SimpleParty getPartyById(UUID partyId) { // großes problem: keine abfrage, ob der spieler aktuell admin ist oder nicht
        return this.partyAndFriendsModule.getPartiesTracker().activeParties().get(partyId);
    }

    public @Nullable SimpleParty getPartyByPlayerId(UUID playerId) {
        CloudPlayer cloudPlayer = this.getCloudPlayerById(playerId);
        if (cloudPlayer == null) {
            return null;
        }
        return getPartyByCloudPlayer(cloudPlayer);
    }

    public @Nullable SimpleParty getPartyByOfflinePlayerId(UUID playerId) {
        CloudOfflinePlayer cloudPlayer = this.partyAndFriendsModule.playerManager().offlinePlayer(playerId);
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
        CloudPlayer cloudPlayer = this.partyAndFriendsModule.playerManager().firstOnlinePlayer(playerName);

        if (cloudPlayer == null) {
            // Send command sender error message
            this.tryToSendMessageToPlayer(content.readUniqueId(),
                    Component.text("Der angebene Spielername wurde nicht gefunden oder war noch nie online",
                            NamedTextColor.RED));
            return;
        }

        switch (partyConstant) {
            case PartyConstants.PARTY_KICK_MESSAGE -> {
                this.kick(cloudPlayer, content.readUniqueId(), false);
            }
            case PartyConstants.PARTY_INVITE_MESSAGE -> {
                this.invite(cloudPlayer, content.readUniqueId());
            }
            case PartyConstants.PARTY_PROMOTE_MESSAGE -> {
                this.promotePlayer(cloudPlayer.uniqueId(), content.readUniqueId(), PromoteReason.COMMAND);
            }
            case PartyConstants.PARTY_JOIN_MESSAGE -> {
                this.join(cloudPlayer, content.readUniqueId());
            }
        }
    }

    public void createParty(@NotNull DataBuf content) {
        UUID playerId = content.readUniqueId();
        CloudPlayer cloudPlayer = this.getCloudPlayerById(playerId);
        if (cloudPlayer == null) {
            return;
        }

        if (this.isInParty(cloudPlayer)) {
            cloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist bereits in einer anderen Party")));
            return; // prevent of creating a party while being in a party
        }

        UUID partyId = UUID.randomUUID();

        cloudPlayer.properties().append(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY, true)
                .append(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY, partyId);
        this.partyAndFriendsModule.playerManager().updateOnlinePlayer(cloudPlayer);

        List<UUID> partyPlayers = new ArrayList<>();
        partyPlayers.add(playerId);

        SimpleParty simpleParty = new SimpleParty(partyId, playerId, partyPlayers, System.currentTimeMillis());

        this.partyAndFriendsModule.getPartiesTracker().activeParties().put(partyId, simpleParty);
        cloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Deine Party wurde erstellt")));
        //cloudPlayer.playerExecutor().sendChatMessage(Component.text("DEBUG: partyId - " + simpleParty.partyId()));

    }

    private void invite(@NotNull CloudPlayer cloudPlayerToInvite, @Nullable UUID senderId) {
        // Step 1: basic checks
        CloudPlayer sender = this.getCloudPlayerById(senderId);
        if (sender == null) {
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
        this.partyAndFriendsModule.playerManager().updateOnlinePlayer(cloudPlayerToInvite);

        String senderName = "unknown";
        CloudPlayer cloudPlayer = this.getCloudPlayerById(senderId);
        if (cloudPlayer != null) {
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
        this.partyAndFriendsModule.getPartiesTracker().activeParties().put(simpleParty.partyId(), simpleParty);

        invitedCloudPlayer.properties().append(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY, true)
                .append(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY, simpleParty.partyId())
                .remove(PartyConstants.PARTY_INVITATIONS_DOCUMENT_PROPERTY);
        this.partyAndFriendsModule.playerManager().updateOnlinePlayer(invitedCloudPlayer);

        invitedCloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Du bist der Party beigetreten")));
        cloudPlayerToJoin.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Der Spieler " + invitedCloudPlayer.name() + " ist deiner Party beigetreten")));

    }

    public void removeFromParty(@NotNull UUID playerId) {
        // First step: remove player
        CloudOfflinePlayer cloudOfflinePlayer = this.getOfflineCloudPlayerById(playerId);
        if (cloudOfflinePlayer == null) {
            return;
        }

        SimpleParty simpleParty = this.getPartyByOfflinePlayerId(playerId);
        if (simpleParty == null) {
            this.tryToSendMessageToPlayer(playerId, Component.text("Du befindest dich in keiner Party",
                    NamedTextColor.RED));
            return;
        }

        // Remove Document properties
        cloudOfflinePlayer.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
        cloudOfflinePlayer.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
        this.partyAndFriendsModule.playerManager().updateOfflinePlayer(cloudOfflinePlayer);

        // Remove from object
        simpleParty.partyMembers().remove(playerId);
        this.partyAndFriendsModule.getPartiesTracker().activeParties().put(simpleParty.partyId(), simpleParty);

        // Check if player is leader. true: try to promote another player
        boolean isLeader = simpleParty.partyLeader().toString().equalsIgnoreCase(playerId.toString());
        if (isLeader) {
            if (simpleParty.partyMembers().size() >= 1) {
                UUID promoteUUID = simpleParty.partyMembers().get(new Random().nextInt(simpleParty.partyMembers().size()));
                this.promotePlayer(promoteUUID,
                        promoteUUID, PromoteReason.FORCE);
            } else {
                // delete party
                this.partyAndFriendsModule.getPartiesTracker().activeParties().remove(simpleParty.partyId());
            }
        }

        this.tryToSendMessageToPlayer(playerId, Component.text("Du hast die Party verlassen"));

    }

    public void promotePlayer(@NotNull UUID playerToPromote, @NotNull UUID senderId, @NotNull PromoteReason promoteReason) {
        SimpleParty simpleParty = this.getPartyByPlayerId(senderId);

        if (simpleParty == null) {
            return;
        }

        if (promoteReason != PromoteReason.FORCE) { // perm check
            if (!this.compareUUID(senderId, simpleParty.partyLeader())) {
                this.tryToSendMessageToPlayer(senderId, Component.text("Du bist kein Partyleader"));
                return;
            }

            if (this.compareUUID(playerToPromote, senderId)) {
                this.tryToSendMessageToPlayer(senderId, Component.text("Du kannst dich nicht selber promoten"));
                return;
            }
        }

        if (this.compareUUID(playerToPromote, simpleParty.partyLeader())) {
            this.tryToSendMessageToPlayer(senderId, Component.text("Du bist bereits der Leader deiner Party"));
            return;
        }

        this.partyAndFriendsModule.getPartiesTracker().activeParties().get(simpleParty.partyId()).partyLeader(playerToPromote); // workaround

        this.tryToSendMessageToPlayer(senderId, Component.text("Du bist nun kein Partyleader mehr"));
        this.tryToSendMessageToPlayer(playerToPromote, Component.text("Du bist nun Partyleader"));

    }

    public boolean compareUUID(UUID uuid, UUID toCompare) {
        return uuid.toString().equalsIgnoreCase(toCompare.toString());
    }

    public void kick(@NotNull CloudPlayer cloudPlayerToKick, @NotNull UUID senderId, boolean force) { // TODO: hier klappt gar nix leck mich am arsch
        SimpleParty simpleParty = this.getPartyByPlayerId(senderId);
        if (simpleParty == null) {
            return;
        }

        if (!this.compareUUID(senderId, simpleParty.partyLeader())) { // perm check if senderId is actual partyLeader
            this.tryToSendMessageToPlayer(senderId, Component.text("Du bist kein Partyleader"));
            return;
        }

        if (!simpleParty.partyMembers().contains(cloudPlayerToKick.uniqueId())) { // joa zu kickender spieler ist nicht in der party
            this.tryToSendMessageToPlayer(senderId, Component.text("Der angegebene Spieler ist nicht in deiner Party"));
            return;
        }

        if (this.compareUUID(cloudPlayerToKick.uniqueId(), senderId)) { // spieler kann sich nicht selber kicken
            this.tryToSendMessageToPlayer(senderId, Component.text("Du kannst dich nicht selber kicken"));
            return;
        }

        // Remove Document properties
        cloudPlayerToKick.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
        cloudPlayerToKick.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
        this.partyAndFriendsModule.playerManager().updateOnlinePlayer(cloudPlayerToKick);

        // Remove from object
        simpleParty.partyMembers().remove(cloudPlayerToKick.uniqueId());
        this.partyAndFriendsModule.getPartiesTracker().activeParties().put(simpleParty.partyId(), simpleParty);

        cloudPlayerToKick.playerExecutor().sendChatMessage(this.partyPrefix
                .append(Component.text("Du wurdest aus der Party gekickt")));
        this.tryToSendMessageToPlayer(senderId, Component.text("Der angegebene Spieler wurde erfolgreich aus der Party gekickt"));

    }

    public void closeParty(@NotNull UUID senderId) {
        CloudPlayer cloudPlayer = this.getCloudPlayerById(senderId);
        if (cloudPlayer == null) {
            return;
        }

        SimpleParty simpleParty = this.getPartyByCloudPlayer(cloudPlayer);
        if (simpleParty == null) {
            return;
        }

        if (!this.compareUUID(senderId, simpleParty.partyLeader())) { // perm check
            cloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du kannst die Party nicht auflösen, da du kein Admin bist")));
            return;
        }

        // Loop through players, delete properties, send message
        for (UUID partyMember : simpleParty.partyMembers()) {
            CloudPlayer loopPlayer = this.getCloudPlayerById(partyMember);
            if (loopPlayer == null) {
                continue;
            }

            loopPlayer.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
            loopPlayer.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
            this.partyAndFriendsModule.playerManager().updateOnlinePlayer(cloudPlayer);

            loopPlayer.playerExecutor().sendChatMessage(this.partyPrefix.append(Component.text("Die Party wurde aufgelöst, du wurdest entfernt")));
        }

        // finally delete reference from map
        this.partyAndFriendsModule.getPartiesTracker().activeParties().remove(simpleParty.partyId());

        cloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix.append(Component.text("Deine Party wurde aufgelöst")));
    }

    public void delete(@NotNull UUID playerId) {
        CloudPlayer cloudPlayer = getCloudPlayerById(playerId);
        if (cloudPlayer == null) {
            return;
        }

        cloudPlayer.properties().remove(PartyConstants.PARTY_UUID_DOCUMENT_PROPERTY);
        cloudPlayer.properties().remove(PartyConstants.PARTY_INVITATIONS_DOCUMENT_PROPERTY);
        cloudPlayer.properties().remove(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY);
        this.partyAndFriendsModule.playerManager().updateOnlinePlayer(cloudPlayer);
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
        if (leaderPlayer != null) {
            leader = this.partyPrefix.append(Component.text("Partyleader: " + leaderPlayer.name()));
            List<UUID> partyMembers = simpleParty.partyMembers();
            partyMembers.remove(simpleParty.partyLeader());

            if (partyMembers.size() == 0) {
                members = this.partyPrefix.append(Component.text("Deine Party ist leer"));
            } else {
                List<String> partyMemberNames = partyMembers.stream().map(uuid -> {
                    CloudPlayer loopPlayer = this.getCloudPlayerById(uuid);
                    String toReturn = "null";

                    if (loopPlayer != null) {
                        toReturn = loopPlayer.name();
                    }

                    return toReturn;
                }).toList();
                members = this.partyPrefix.append(Component.text("Mitglieder: " + String.join(", ", partyMemberNames)));
            }
        }

        cloudPlayer.playerExecutor().sendChatMessage(leader.append(Component.newline().append(members)));
    }

    public void chat(@NotNull DataBuf content) {
        UUID senderId = content.readUniqueId();
        String message = content.readString();

        CloudPlayer cloudPlayer = this.getCloudPlayerById(senderId);
        if (cloudPlayer == null) {
            return;
        }

        if (!this.isInParty(cloudPlayer)) {
            cloudPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text("Du bist in keiner Party oder deine Party wurde nicht gefunden")));
            return;
        }

        // Try to send every player a message
        SimpleParty simpleParty = this.getPartyByCloudPlayer(cloudPlayer);
        if (simpleParty == null) {
            return; // sollte eigentlich nicht passieren, siehe check oben
        }

        for (UUID partyMember : simpleParty.partyMembers()) {
            CloudPlayer partyMemberPlayer = this.getCloudPlayerById(partyMember);
            if (partyMemberPlayer == null) {
                continue;
            }

            partyMemberPlayer.playerExecutor().sendChatMessage(this.partyPrefix
                    .append(Component.text(cloudPlayer.name(), NamedTextColor.GREEN))
                    .append(Component.text(" > ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(message, NamedTextColor.WHITE)));
        }

    }

    public enum PromoteReason {
        COMMAND, FORCE
    }

}
