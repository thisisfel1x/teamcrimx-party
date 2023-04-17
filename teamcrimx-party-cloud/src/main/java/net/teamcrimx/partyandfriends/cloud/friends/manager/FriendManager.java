package net.teamcrimx.partyandfriends.cloud.friends.manager;

import eu.cloudnetservice.modules.bridge.player.CloudOfflinePlayer;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.teamcrimx.partyandfriends.api.constants.ChatConstants;
import net.teamcrimx.partyandfriends.api.database.MongoCollection;
import net.teamcrimx.partyandfriends.api.friends.FriendConstants;
import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;
import net.teamcrimx.partyandfriends.cloud.SimpleManager;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Iterator;
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

    // TODO: overhaul
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
                    joinMessage = "Aktuell sind keine Freunde online " + simpleFriend.onlineFriends().size();
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

        CloudOfflinePlayer cloudPlayerToAdd = this.getOfflineCloudPlayerByName(playerNameToAdd);
        if(cloudPlayerToAdd == null) {
            this.tryToSendMessageToPlayer(senderUUID,
                    Component.text("Dieser Spieler war noch nie online", NamedTextColor.RED));
            return;
        }

        SimpleFriend simpleFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        if(simpleFriend.friends().contains(cloudPlayerToAdd.uniqueId())) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du bist bereits mit diesem Spieler befreundet",
                    NamedTextColor.RED));
            return;
        }

        SimpleFriend simpleFriendToAdd = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(cloudPlayerToAdd.uniqueId());
        if(simpleFriendToAdd == null) {
            try {
                simpleFriendToAdd = SimpleFriend.getSimpleFriendByUUID(cloudPlayerToAdd.uniqueId()).get();
            } catch (InterruptedException | ExecutionException ignored) {
                // TODO: fehler
            }
        }

        if (simpleFriendToAdd == null) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("ich hasse dich. das spielerobjekt wurde nicht gefunden"));
            return;
        }

        if(simpleFriendToAdd.friendRequests().contains(senderUUID)) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du hast diesem Spieler bereits eine " +
                    "Freundschaftsanfrage gesendet", NamedTextColor.RED));
            return;
        }

        // add senderuuid to list
        simpleFriendToAdd.friendRequests().add(senderUUID);
        simpleFriendToAdd.update(true);

        this.tryToSendMessageToPlayer(senderUUID,
                Component.text("Eine Freundschaftsanfrage wurde an " + cloudPlayerToAdd.name() + " versandt",
                        NamedTextColor.GREEN));
        /*tryToSendMessageToPlayer(cloudPlayerToAdd.uniqueId(),
                Component.text("Du hast eine Freundschaftsanfrage von " + senderPlayer.name() + " erhalten",
                        NamedTextColor.GRAY));
        */
        Component a = Component.text("Du hast eine Freundschaftsanfrage von" + senderPlayer.name() + " erhalten. Klicke zum ", NamedTextColor.GRAY);
        Component b = Component.text("ANNEHMEN", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + senderPlayer.name()));
        Component c = Component.text("ABLEHNEN", NamedTextColor.RED)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + senderPlayer.name()));

        this.tryToSendMessageToPlayer(cloudPlayerToAdd.uniqueId(),
                Component.textOfChildren(a, b, Component.text(" "), c));
    }

    public void executeSingle(UUID senderUUID, String playerName, String friendConstant) {
        if(senderUUID == null || playerName == null) {
            return;
        }

        CloudPlayer senderPlayer = this.getCloudPlayerById(senderUUID);
        if(senderPlayer == null) {
            return;
        }
        SimpleFriend senderFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        CloudOfflinePlayer playerToInteract = this.getOfflineCloudPlayerByName(playerName);
        if(playerToInteract == null) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Es ist ein Fehler aufgetreten",
                    NamedTextColor.RED));
            return;
        }
        SimpleFriend friendToInteract = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(playerToInteract.uniqueId());
        if(friendToInteract == null) {
            try {
                friendToInteract = SimpleFriend.getSimpleFriendByUUID(playerToInteract.uniqueId()).get();
            } catch (InterruptedException | ExecutionException e) {
                this.tryToSendMessageToPlayer(senderUUID, Component.text("Es ist ein Fehler aufgetreten",
                        NamedTextColor.RED));
                return;
            }
        }

        if((friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_ACCEPT_MESSAGE)
                || friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_DENY_MESSAGE))
                && !senderFriend.friendRequests().contains(playerToInteract.uniqueId())) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du hast von diesem Spieler keine Freundschaftsanfrage erhalten",
                    NamedTextColor.RED));
            return;
        } else if (friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_REMOVE_MESSAGE)
                && !senderFriend.friends().contains(playerToInteract.uniqueId())) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du bist mit diesem Spieler nicht befreundet",
                    NamedTextColor.RED));
            return;
        }

        if (friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_ACCEPT_MESSAGE)) {
            senderFriend.friendRequests().remove(playerToInteract.uniqueId());
            senderFriend.friends().add(playerToInteract.uniqueId());
            senderFriend.update(true);

            friendToInteract.friendRequests().remove(senderPlayer.uniqueId());
            friendToInteract.friends().add(senderPlayer.uniqueId());
            friendToInteract.update(true);

            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du bist nun mit " + playerToInteract.name() + " befreundet"));
            this.tryToSendMessageToPlayer(playerToInteract.uniqueId(), Component.text(senderPlayer.name() + " hat deine Freundschaftsanfrage aktzeptiert"));

        } else if (friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_DENY_MESSAGE)) {
            senderFriend.friendRequests().remove(playerToInteract.uniqueId());
            senderFriend.update(true);

            friendToInteract.friendRequests().remove(senderPlayer.uniqueId());
            friendToInteract.update(true);

            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du hast die Freundschaftsanfrage von " + playerToInteract.name() + " abgelehnt"));
            this.tryToSendMessageToPlayer(playerToInteract.uniqueId(), Component.text(senderPlayer.name() + " hat deine Freundschaftsanfrage abgelehnt"));

        } else if (friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_REMOVE_MESSAGE)) {
            senderFriend.friends().remove(playerToInteract.uniqueId());
            senderFriend.update(true);

            friendToInteract.friends().remove(senderPlayer.uniqueId());
            friendToInteract.update(true);

            this.tryToSendMessageToPlayer(senderUUID, Component.text("Die Freundschaft mit " + playerToInteract.name() + " wurde aufgelöst"));
            this.tryToSendMessageToPlayer(playerToInteract.uniqueId(), Component.text("Die Freundschaft mit " + senderPlayer.name() + " wurde aufgelöst"));

        }
    }

    public void executeToAll(UUID senderUUID, String friendConstant) {
        if(senderUUID == null) {
            return;
        }

        CloudPlayer senderPlayer = this.getCloudPlayerById(senderUUID);
        if(senderPlayer == null) {
            return;
        }
        SimpleFriend senderFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        ArrayList<UUID> entries = (friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_REMOVE_MESSAGE)
                ? senderFriend.friends() : senderFriend.friendRequests());

        for (Iterator<UUID> iterator = entries.iterator(); iterator.hasNext();) {
            UUID friendListEntry = iterator.next();
            CloudOfflinePlayer cloudOfflinePlayer = this.getOfflineCloudPlayerById(friendListEntry);
            if(cloudOfflinePlayer == null) {
                continue;
            }

            this.executeSingle(senderUUID, cloudOfflinePlayer.name(), friendConstant);
        }
    }
}
