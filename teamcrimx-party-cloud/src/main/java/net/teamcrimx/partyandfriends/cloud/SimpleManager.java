package net.teamcrimx.partyandfriends.cloud;

import eu.cloudnetservice.modules.bridge.player.CloudOfflinePlayer;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SimpleManager {

    private Component prefix = Component.text("not set");

    private final PartyAndFriendsModule partyAndFriendsModule;

    public SimpleManager(PartyAndFriendsModule partyAndFriendsModule) {
        this.partyAndFriendsModule = partyAndFriendsModule;
    }

    public @Nullable CloudPlayer getCloudPlayerById(UUID playerId) {
        return this.partyAndFriendsModule.playerManager().onlinePlayer(playerId);
    }

    public @Nullable CloudOfflinePlayer getOfflineCloudPlayerById(UUID playerId) {
        return this.partyAndFriendsModule.playerManager().offlinePlayer(playerId);
    }

    public void tryToSendMessageToPlayer(UUID playerId, Component message) {
        CloudPlayer cloudPlayer = this.getCloudPlayerById(playerId);

        if (cloudPlayer == null) {
            return;
        }

        cloudPlayer.playerExecutor().sendChatMessage(this.prefix.append(message));
    }

    public void prefix(Component prefix) {
        this.prefix = prefix;
    }
}
