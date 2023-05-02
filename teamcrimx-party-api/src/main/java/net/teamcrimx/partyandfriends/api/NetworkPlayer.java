package net.teamcrimx.partyandfriends.api;

import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.player.CloudOfflinePlayer;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import eu.cloudnetservice.modules.bridge.player.NetworkServiceInfo;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.teamcrimx.partyandfriends.api.cloud.CloudUtils;

import java.util.UUID;

public class NetworkPlayer {

    private final PlayerManager playerManager;
    private final UUID uuid;
    private String name;
    private final Component chatColor;
    private final Component formattedName;
    private final Component rank;

    public NetworkPlayer(UUID uuid) {
        this.playerManager = ServiceRegistry.first(PlayerManager.class);

        this.uuid = uuid;

        CloudOfflinePlayer cloudOfflinePlayer = this.playerManager.offlinePlayer(this.uuid);
        if(cloudOfflinePlayer != null) {
            name = cloudOfflinePlayer.name();
        }

        this.chatColor = CloudUtils.getDisplayColor(this.uuid);
        this.formattedName = Component.text(name, this.chatColor.color());
        this.rank = CloudUtils.getRank(this.uuid);

    }

    public String name() {
        return name;
    }

    public UUID uuid() {
        return uuid;
    }

    public Component chatColor() {
        return chatColor;
    }

    public Component formattedName() {
        return formattedName;
    }

    public Component rank() {
        return rank;
    }

    public boolean isOnline() {
        return this.playerManager.onlinePlayer(this.uuid) != null;
    }

    public String connectedServer() {
        String toReturn = "Offline";
        CloudPlayer cloudPlayer = this.playerManager.onlinePlayer(this.uuid);

        if(cloudPlayer == null) {
            return toReturn;
        }

        NetworkServiceInfo networkServiceInfo = cloudPlayer.connectedService();
        if(networkServiceInfo == null) {
            return toReturn;
        }

        return networkServiceInfo.serverName();
    }

}
