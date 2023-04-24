package net.teamcrimx.partyandfriends.cloud.friends.manager;

import eu.cloudnetservice.modules.bridge.player.CloudOfflinePlayer;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.TriState;
import net.teamcrimx.partyandfriends.api.constants.ChatConstants;
import net.teamcrimx.partyandfriends.api.database.MongoCollection;
import net.teamcrimx.partyandfriends.api.friends.FriendConstants;
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

    // TODO: overhaul
    public void checkForDatabaseAndInitializeFriends(UUID uniqueId) {
        if (!this.partyAndFriendsModule.mongoMethods().doesExists(uniqueId, MongoCollection.FRIENDS)) {
            this.createFriendDocument(uniqueId);
            this.tryToSendMessageToPlayer(uniqueId, Component.text("Dein Profil wurde erfolgreich angelegt",
                    NamedTextColor.GREEN));
            this.checkForDatabaseAndInitializeFriends(uniqueId);
        } else {
            try {
                SimpleFriend simpleFriend = SimpleFriend.getSimpleFriendByUUID(uniqueId).get();
                if (simpleFriend == null) {
                    this.sendErrorMessage(uniqueId, "INIT_ERROR");
                    return;
                }

                this.partyAndFriendsModule.friendHolder().simpleFriendMap().put(uniqueId, simpleFriend);

                int onlineFriends = simpleFriend.onlineFriends().size();
                String joinMessage = String.format("<gray>Aktuell sind <green>%s <gray>Freunde online", onlineFriends);
                if (onlineFriends == 0) {
                    joinMessage = "<gray>Aktuell sind keine Freunde online";
                } else if (onlineFriends == 1) {
                    joinMessage = "<gray>Aktuell ist <green>ein <gray>Freund online";
                }

                this.tryToSendMessageToPlayer(uniqueId, this.partyAndFriendsModule.miniMessage().deserialize(joinMessage));

                if (simpleFriend.friendRequests().size() > 0) {
                    this.tryToSendMessageToPlayer(uniqueId, Component.text("Du hast offene Freundschaftsanfragen",
                            NamedTextColor.GREEN));
                }

                this.notifyFriends(uniqueId, NotifyType.ONLINE);

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
        if (senderUUID == null || playerNameToAdd == null) {
            return;
        }

        CloudPlayer senderPlayer = this.getCloudPlayerById(senderUUID);
        if (senderPlayer == null) {
            this.sendErrorMessage(senderUUID, "NULL_PLAYER");
            return;
        }

        CloudOfflinePlayer cloudPlayerToAdd = this.getOfflineCloudPlayerByName(playerNameToAdd);
        if (cloudPlayerToAdd == null) {
            this.tryToSendMessageToPlayer(senderUUID,
                    Component.text("Dieser Spieler war noch nie online", NamedTextColor.RED));
            return;
        }

        if (senderUUID.toString().equalsIgnoreCase(cloudPlayerToAdd.uniqueId().toString())) {
            this.tryToSendMessageToPlayer(senderUUID,
                    Component.text("Du kannst dich nicht selbst hinzufügen", NamedTextColor.RED));
            return;
        }

        SimpleFriend simpleFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        if (simpleFriend.friends().contains(cloudPlayerToAdd.uniqueId())) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du bist bereits mit diesem Spieler befreundet",
                    NamedTextColor.RED));
            return;
        }

        SimpleFriend simpleFriendToAdd = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(cloudPlayerToAdd.uniqueId());
        if (simpleFriendToAdd == null) {
            try {
                simpleFriendToAdd = SimpleFriend.getSimpleFriendByUUID(cloudPlayerToAdd.uniqueId()).get();
            } catch (InterruptedException | ExecutionException ignored) {
                this.sendErrorMessage(senderUUID, "PLAYER_NOT_FOUND");
            }
        }

        if (simpleFriendToAdd == null) {
            this.sendErrorMessage(senderUUID, "NULL_PLAYER");
            return;
        }

        if (simpleFriendToAdd.friendRequests().contains(senderUUID)) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du hast diesem Spieler bereits eine " +
                    "Freundschaftsanfrage gesendet", NamedTextColor.RED));
            return;
        }

        // add senderuuid to list
        simpleFriendToAdd.friendRequests().add(senderUUID);
        simpleFriendToAdd.update(true);

        this.tryToSendMessageToPlayer(senderUUID,
                Component.textOfChildren(Component.text("Eine Freundschaftsanfrage wurde an ", NamedTextColor.GRAY),
                        simpleFriendToAdd.formattedName(), Component.text(" versand", NamedTextColor.GRAY)));

        Component a = Component.textOfChildren(Component.text("Du hast eine Freundschaftsanfrage von ", NamedTextColor.GRAY),
                simpleFriend.formattedName(), Component.text(" bekommen", NamedTextColor.GRAY));
        Component b = Component.text("ANNEHMEN", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + senderPlayer.name()));
        Component c = Component.text("ABLEHNEN", NamedTextColor.RED)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + senderPlayer.name()));

        this.tryToSendMessageToPlayer(cloudPlayerToAdd.uniqueId(),
                Component.textOfChildren(a, b, Component.text(" | ", NamedTextColor.DARK_GRAY), c));
    }

    public void executeSingle(UUID senderUUID, String playerName, String friendConstant) {
        if (senderUUID == null || playerName == null) {
            return;
        }

        CloudPlayer senderPlayer = this.getCloudPlayerById(senderUUID);
        if (senderPlayer == null) {
            return;
        }
        SimpleFriend senderFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        CloudOfflinePlayer playerToInteract = this.getOfflineCloudPlayerByName(playerName);
        if (playerToInteract == null) {
            this.sendErrorMessage(senderUUID, "NULL_PLAYER");
            return;
        }
        SimpleFriend friendToInteract = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(playerToInteract.uniqueId());
        if (friendToInteract == null) {
            try {
                friendToInteract = SimpleFriend.getSimpleFriendByUUID(playerToInteract.uniqueId()).get();
            } catch (InterruptedException | ExecutionException e) {
                this.sendErrorMessage(senderUUID, "PLAYER_NOT_FOUND");
                return;
            }
        }

        if ((friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_ACCEPT_MESSAGE)
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
            //senderFriend.onlineFriendsCache().put(playerToInteract.uniqueId(), TriState.NOT_SET);
            senderFriend.update(true);

            friendToInteract.friendRequests().remove(senderPlayer.uniqueId());
            friendToInteract.friends().add(senderPlayer.uniqueId());
            //friendToInteract.onlineFriendsCache().put(senderPlayer.uniqueId(), TriState.NOT_SET);
            friendToInteract.update(true);

            this.tryToSendMessageToPlayer(senderUUID, Component.textOfChildren(Component.text("Du bist nun mit ", NamedTextColor.GRAY),
                    friendToInteract.formattedName(), Component.text(" befreundet", NamedTextColor.GRAY)));
            this.tryToSendMessageToPlayer(playerToInteract.uniqueId(), Component.textOfChildren(senderFriend.formattedName(),
                    Component.text(" hat deine Freundschaftsanfrage aktzeptiert", NamedTextColor.GRAY)));

        } else if (friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_DENY_MESSAGE)) {
            senderFriend.friendRequests().remove(playerToInteract.uniqueId());
            senderFriend.update(true);

            friendToInteract.friendRequests().remove(senderPlayer.uniqueId());
            friendToInteract.update(true);

            this.tryToSendMessageToPlayer(senderUUID, Component.textOfChildren(Component.text("Du hast die Freundschaftsanfrage von ", NamedTextColor.GRAY),
                    friendToInteract.formattedName(), Component.text(" abgelehnt", NamedTextColor.RED)));
            this.tryToSendMessageToPlayer(playerToInteract.uniqueId(), Component.textOfChildren(senderFriend.formattedName(),
                    Component.text(" hat deine Freundschaftsanfrage abgelehnt", NamedTextColor.GRAY)));

        } else if (friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_REMOVE_MESSAGE)) {
            senderFriend.friends().remove(playerToInteract.uniqueId());
            //senderFriend.onlineFriendsCache().invalidate(playerToInteract.uniqueId());
            senderFriend.update(true);

            friendToInteract.friends().remove(senderPlayer.uniqueId());
            //friendToInteract.onlineFriendsCache().invalidate(senderPlayer.uniqueId());
            friendToInteract.update(true);

            this.tryToSendMessageToPlayer(senderUUID, Component.textOfChildren(Component.text("Die Freundschaft mit ", NamedTextColor.GRAY),
                    friendToInteract.formattedName(), Component.text(" wurde aufgelöst", NamedTextColor.GRAY)));
            this.tryToSendMessageToPlayer(playerToInteract.uniqueId(), Component.textOfChildren(Component.text("Die Freundschaft mit ", NamedTextColor.GRAY),
                    senderFriend.formattedName(), Component.text(" wurde aufgelöst", NamedTextColor.GRAY)));

        }
    }

    public void executeToAll(UUID senderUUID, String friendConstant) {
        if (senderUUID == null) {
            return;
        }

        CloudPlayer senderPlayer = this.getCloudPlayerById(senderUUID);
        if (senderPlayer == null) {
            return;
        }
        SimpleFriend senderFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        ArrayList<UUID> entries = (friendConstant.equalsIgnoreCase(FriendConstants.FRIEND_REMOVE_MESSAGE)
                ? new ArrayList<>(senderFriend.friends()) : new ArrayList<>(senderFriend.friendRequests()));

        for (UUID friendListEntry : entries) {
            CloudOfflinePlayer cloudOfflinePlayer = this.getOfflineCloudPlayerById(friendListEntry);
            if (cloudOfflinePlayer == null) {
                continue;
            }

            try {
                this.executeSingle(senderUUID, cloudOfflinePlayer.name(), friendConstant);
            } catch (Exception ignored) {
                this.sendErrorMessage(senderUUID, "REMOVEALL");
            }
        }
    }

    public void listFriends(UUID senderUUID) {
        if (senderUUID == null) {
            return;
        }

        SimpleFriend simpleFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        if (simpleFriend.onlineFriends().size() == 0) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Aktuell sind keine Freunde online",
                    NamedTextColor.GRAY));
        } else {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Diese Freunde sind aktuell online",
                    NamedTextColor.GRAY));

            simpleFriend.onlineFriendsCache().asMap().forEach((uuid, triState) -> {
                if (triState != TriState.TRUE) {
                    return;
                }

                CloudPlayer loopPlayer = this.getCloudPlayerById(uuid);
                if (loopPlayer == null) {
                    return;
                }

                SimpleFriend simpleLoopFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(uuid);

                String onlineServer = loopPlayer.connectedService().serverName();
                this.tryToSendMessageToPlayer(senderUUID, Component.textOfChildren(simpleLoopFriend.formattedName(),
                        Component.text(" befindet sich auf ", NamedTextColor.GRAY),
                        Component.text(onlineServer, TextColor.fromHexString("#4DA8FB"))
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/friend jump " + loopPlayer.name()))
                                .hoverEvent(HoverEvent.showText(Component.text("Klicke zum verbinden",
                                        NamedTextColor.GREEN)))));
            });

        }
    }

    public void listFriendRequests(UUID senderUUID) {
        if (senderUUID == null) {
            return;
        }

        SimpleFriend simpleFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        if (simpleFriend.friendRequests().size() == 0) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du hast keine offenen Freundschaftsanfragen",
                    NamedTextColor.GRAY));
        } else {
            List<TextComponent> names = simpleFriend.friendRequests().stream().map(uuid -> {
                CloudOfflinePlayer cloudOfflinePlayer = this.getOfflineCloudPlayerById(uuid);
                if (cloudOfflinePlayer == null) {
                    return Component.text("null");
                }
                return Component.text(cloudOfflinePlayer.name(), NamedTextColor.YELLOW)
                        .hoverEvent(HoverEvent.showText(Component.text("Klicke zum annehmen", NamedTextColor.GREEN)))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/friend add " + cloudOfflinePlayer.name()));
            }).toList();
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Folgende Freundschaftsanfragen stehen aus", NamedTextColor.GRAY));

            Component toSend = Component.join(JoinConfiguration.builder()
                    .separator(Component.text(", ", NamedTextColor.DARK_GRAY)).build(), names);

            this.tryToSendMessageToPlayer(senderUUID, toSend);
        }
    }

    public void jumpToPlayer(UUID senderUUID, String playerName) {
        if (senderUUID == null) {
            return;
        }

        CloudPlayer senderPlayer = this.getCloudPlayerById(senderUUID);
        if (senderPlayer == null) {
            return;
        }

        CloudPlayer playerToConnect = this.partyAndFriendsModule.playerManager().firstOnlinePlayer(playerName);
        if (playerToConnect == null) {
            this.sendErrorMessage(senderUUID, "PLAYER_NOT_FOUND");
            return;
        }

        String connectedServer = playerToConnect.connectedService().serverName();

        this.tryToSendMessageToPlayer(senderUUID,
                Component.textOfChildren(Component.text("Versuche auf Server ", NamedTextColor.GRAY),
                        Component.text(connectedServer,
                                TextColor.fromHexString("#58A5FA")),
                        Component.text(" zu verbinden", NamedTextColor.GRAY)));

        senderPlayer.playerExecutor().connect(connectedServer);
    }

    public void notifyFriends(UUID senderUUID, NotifyType notifyType) {
        SimpleFriend simpleFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        Component toSend = Component.textOfChildren(simpleFriend.formattedName(),
                Component.text(" ist nun ", NamedTextColor.GRAY),
                (notifyType == NotifyType.ONLINE ? Component.text("online", NamedTextColor.GREEN)
                        : Component.text("offline", NamedTextColor.RED)));

        for (UUID onlineFriend : simpleFriend.onlineFriends()) {
            this.tryToSendMessageToPlayer(onlineFriend, toSend);
        }

    }

    public void messagePlayer(UUID senderUUID, String playerName, String message) {
        SimpleFriend senderFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(senderUUID);

        CloudPlayer receiver = this.partyAndFriendsModule.playerManager().firstOnlinePlayer(playerName);
        if (receiver == null) {
            this.sendErrorMessage(senderUUID, "NULL");
            return;
        }

        if (senderUUID.toString().equalsIgnoreCase(receiver.uniqueId().toString())) {
            this.tryToSendMessageToPlayer(senderUUID, Component.text("Du kannst dir nicht selber Nachrichten schreiben", NamedTextColor.RED));
            return;
        }

        SimpleFriend receiverFriend = this.partyAndFriendsModule.friendHolder().simpleFriendMap().get(receiver.uniqueId());

        Component senderMessage = Component.join(JoinConfiguration.builder().build(),
                Component.text("Du", NamedTextColor.GREEN), Component.text(" » ", NamedTextColor.DARK_GRAY),
                receiverFriend.formattedName(), Component.text(": ", NamedTextColor.DARK_GRAY),
                Component.text(message, NamedTextColor.WHITE));

        this.tryToSendMessageToPlayer(senderUUID, senderMessage);

        Component receiverMessage = Component.join(JoinConfiguration.builder().build(),
                        senderFriend.formattedName(), Component.text(" » ", NamedTextColor.DARK_GRAY),
                        Component.text("Du", NamedTextColor.GREEN), Component.text(": ", NamedTextColor.DARK_GRAY),
                        Component.text(message, NamedTextColor.WHITE))
                .hoverEvent(HoverEvent.showText(Component.text("Klicke zum antworten", NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.suggestCommand("/msg " + senderFriend.name()));

        this.tryToSendMessageToPlayer(receiverFriend.uuid(), receiverMessage);

    }

    public enum NotifyType {
        ONLINE, OFFLINE
    }

}
