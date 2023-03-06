package net.teamcrimx.partyandfriends.cloud.manager;

import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.teamcrimx.partyandfriends.api.constants.ChatConstants;
import net.teamcrimx.partyandfriends.api.database.MongoCollection;
import net.teamcrimx.partyandfriends.api.database.MongoDatabaseImpl;
import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;
import net.teamcrimx.partyandfriends.cloud.SimpleManager;
import org.bson.Document;

import java.util.ArrayList;
import java.util.UUID;

public class FriendManager extends SimpleManager {

    private final PartyAndFriendsModule partyAndFriendsModule;

    private final Component friendPrefix = ChatConstants.friendPrefix;

    public FriendManager(PartyAndFriendsModule partyAndFriendsModule) {
        super(partyAndFriendsModule);
        this.partyAndFriendsModule = partyAndFriendsModule;

        this.prefix(friendPrefix);
    }

    public void addFriend(UUID senderUUID, String playerNameToAdd) {
        if(senderUUID == null || playerNameToAdd == null) {
            return;
        }

        CloudPlayer senderPlayer = this.getCloudPlayerById(senderUUID);
        if(senderPlayer == null) {
            return;
        }
    }

    public void checkForDatabaseAndInitializeFriends(UUID uniqueId) {
        if(!this.partyAndFriendsModule.mongoMethods().doesExists(uniqueId, MongoCollection.FRIENDS)) {
            this.createFriendDocument(uniqueId);
            this.tryToSendMessageToPlayer(uniqueId, Component.text("deine daten sind jetzt eingetragen"));
        } else {
            this.tryToSendMessageToPlayer(uniqueId, Component.text("keiner deiner freunde ist online du schwanz"));
            SimpleFriend.getSimpleFriendByUUID(uniqueId).thenAccept(friend -> {
                this.tryToSendMessageToPlayer(friend.uuid(), Component.text("alle freunde: " + friend.friends().size()));
                this.tryToSendMessageToPlayer(friend.uuid(), Component.text("frineds online: " + friend.onlineFriends().size()));
            });
        }
    }

    private void createFriendDocument(UUID uniqueId) {
        Document friendDocument = new Document("_id", uniqueId.toString())
                .append("friends", new ArrayList<>());

        this.partyAndFriendsModule.mongoMethods().insertDocumentSync(friendDocument, MongoCollection.FRIENDS);
    }
}
