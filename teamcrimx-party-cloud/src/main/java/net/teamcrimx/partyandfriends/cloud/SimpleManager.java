package net.teamcrimx.partyandfriends.cloud;

import eu.cloudnetservice.modules.bridge.player.CloudOfflinePlayer;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    public @Nullable CloudOfflinePlayer getOfflineCloudPlayerByName(String name) {
        return this.partyAndFriendsModule.playerManager().firstOfflinePlayer(name);
    }

    protected void tryToSendMessageToPlayer(UUID playerId, Component message) {
        CloudPlayer cloudPlayer = this.getCloudPlayerById(playerId);

        if (cloudPlayer == null) {
            return;
        }

        cloudPlayer.playerExecutor().sendChatMessage(Component.textOfChildren(this.prefix, message));
    }

    public void prefix(Component prefix) {
        this.prefix = prefix;
    }

    protected void sendErrorMessage(UUID uniqueId, @Nullable String debug) {
        this.tryToSendMessageToPlayer(uniqueId,
                Component.text("Es ist ein Fehler aufgetreten" + (debug != null ? (" (" + debug + ")") : ""),
                        NamedTextColor.RED));
    }
}
