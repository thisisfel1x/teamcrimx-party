package net.teamcrimx.partyandfriends.cloud.friends.manager;

import eu.cloudnetservice.modules.bridge.player.CloudOfflinePlayer;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.teamcrimx.partyandfriends.api.constants.ChatConstants;
import net.teamcrimx.partyandfriends.api.database.MongoCollection;
import net.teamcrimx.partyandfriends.api.database.MongoDatabaseImpl;
import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;
import net.teamcrimx.partyandfriends.cloud.SimpleManager;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FriendManager extends SimpleManager {

    private final PartyAndFriendsModule partyAndFriendsModule;

    private final Component friendPrefix = ChatConstants.friendPrefix;

    public FriendManager(PartyAndFriendsModule partyAndFriendsModule) {
        super(partyAndFriendsModule);
        this.partyAndFriendsModule = partyAndFriendsModule;

        this.prefix(friendPrefix);
    }

    public void checkForDatabaseAndInitializeFriends(UUID uniqueId) {
        if(!this.partyAndFriendsModule.mongoMethods().doesExists(uniqueId, MongoCollection.FRIENDS)) {
            this.createFriendDocument(uniqueId);
            this.tryToSendMessageToPlayer(uniqueId, Component.text("deine daten sind jetzt eingetragen",
                    NamedTextColor.GRAY));
            this.checkForDatabaseAndInitializeFriends(uniqueId);
        } else {
            try {
                SimpleFriend simpleFriend = SimpleFriend.getSimpleFriendByUUID(uniqueId).get();
                if(simpleFriend == null) {
                    // TODO: error
                    return;
                }

                this.partyAndFriendsModule.friendHolder().simpleFriendMap().put(uniqueId, simpleFriend);

                this.tryToSendMessageToPlayer(uniqueId, Component.text("Dein Freundeprofil wurde geladen",
                        NamedTextColor.GRAY));

                int onlineFriends = simpleFriend.onlineFriends().size();
                String joinMessage = String.format("Aktuell sind %s Freunde online", onlineFriends);
                if(onlineFriends == 0) {
                    joinMessage = "Aktuell sind keine Freunde online";
                } else if (onlineFriends == 1) {
                    joinMessage = "Aktuell ist ein Freund online";
                }

                this.tryToSendMessageToPlayer(uniqueId,
                        Component.text(joinMessage, NamedTextColor.GRAY));

                if(simpleFriend.friendRequests().size() > 0) {
                    this.tryToSendMessageToPlayer(uniqueId, Component.text("Du hast offene Freundschaftsanfragen",
                            NamedTextColor.GREEN));
                }

            } catch (InterruptedException | ExecutionException ignored) {
                // TODO: send message
            }
        }
    }

    private void createFriendDocument(UUID uniqueId) {
        Document friendDocument = new Document("_id", uniqueId.toString())
                .append("friends", new ArrayList<>())
                .append("friendRequests", new ArrayList<>());

        this.partyAndFriendsModule.mongoMethods().insertDocumentSync(friendDocument, MongoCollection.FRIENDS);
    }

    public void addFriend(UUID senderUUID, String playerNameToAdd) {
        if(senderUUID == null || playerNameToAdd == null) {
            return;
        }

        CloudPlayer senderPlayer = this.getCloudPlayerById(senderUUID);
        if(senderPlayer == null) {
            return;
        }

        SimpleFriend simpleFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        CloudOfflinePlayer cloudPlayerToAdd = this.getOfflineCloudPlayerByName(playerNameToAdd);
        if(cloudPlayerToAdd == null) {
            this.tryToSendMessageToPlayer(senderUUID,
                    Component.text("Dieser Spieler war noch nie online", NamedTextColor.RED));
            return;
        }

        SimpleFriend simpleFriendToAdd = null;
        try {
            simpleFriendToAdd = SimpleFriend.getSimpleFriendByUUID(cloudPlayerToAdd.uniqueId()).get();
        } catch (InterruptedException | ExecutionException ignored) {
            // TODO: fehler
        }

        if (simpleFriendToAdd == null) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("ich hasse dich. das spielerobjekt wurde nicht gefunden"));
            return;
        }

        // add uuid to friend requests
        List<String> friendRequests = this.partyAndFriendsModule.mongoMethods()
                .getStringArrayListFromDocumentSync(cloudPlayerToAdd.uniqueId(), MongoCollection.FRIENDS, "friendRequests");

        if(friendRequests == null) {
            return; // TODO : error
        }

        if(friendRequests.contains(senderUUID.toString())) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du hast diesem Spieler bereits eine " +
                    "Freundschaftsanfrage gesendet", NamedTextColor.RED));
            return;
        }

        // add senderuuid to list
        friendRequests.add(senderUUID.toString());

        // update object
        this.partyAndFriendsModule.mongoMethods().insert(cloudPlayerToAdd.uniqueId(), "friendRequests", friendRequests,
                MongoCollection.FRIENDS);

        this.tryToSendMessageToPlayer(senderUUID,
                Component.text("Eine Freundschaftsanfrage wurde an " + cloudPlayerToAdd.name() + " versand",
                        NamedTextColor.GREEN));
        tryToSendMessageToPlayer(cloudPlayerToAdd.uniqueId(),
                Component.text("Du hast eine Freundschaftsanfrage von " + senderPlayer.name() + " erhalten",
                        NamedTextColor.GRAY));

    }

    public void acceptFriend(UUID senderUUID, String playerNameToAccept) {
        if(senderUUID == null || playerNameToAccept == null) {
            return;
        }

        CloudPlayer senderPlayer = this.getCloudPlayerById(senderUUID);
        if(senderPlayer == null) {
            return;
        }

        CloudOfflinePlayer playerToAccept = this.getOfflineCloudPlayerByName(playerNameToAccept);
        if(playerToAccept == null) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Es ist ein Fehler aufgetreten",
                    NamedTextColor.RED));
            return;
        }

        List<String> friendRequests = this.partyAndFriendsModule.mongoMethods()
                .getStringArrayListFromDocumentSync(senderPlayer.uniqueId(), MongoCollection.FRIENDS, "friendRequests");

        if(friendRequests == null) {
            return; // TODO : error
        }

        if(!friendRequests.contains(playerToAccept.toString())) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du hast von diesem Spieler keine Freundschaftsanfrage erhalten",
                    NamedTextColor.RED));
            return;
        }

        friendRequests.remove(playerToAccept.toString());
        this.partyAndFriendsModule.mongoMethods().insert(senderUUID, "friendRequests", friendRequests, MongoCollection.FRIENDS);

        // other side
        friendRequests = this.partyAndFriendsModule.mongoMethods()
                .getStringArrayListFromDocumentSync(playerToAccept.uniqueId(), MongoCollection.FRIENDS, "friendRequests");

        if(friendRequests == null) {
            return; // TODO : error
        }

        if(friendRequests.contains(senderUUID.toString())) {
            friendRequests.remove(senderPlayer.toString());
        }

        this.partyAndFriendsModule.mongoMethods().insert(senderPlayer.uniqueId(), "friendRequests", friendRequests,
                MongoCollection.FRIENDS);

    }
}
