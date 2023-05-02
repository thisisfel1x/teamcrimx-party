package net.teamcrimx.partyandfriends.paper;

import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.ext.platforminject.api.PlatformEntrypoint;
import eu.cloudnetservice.ext.platforminject.api.stereotype.PlatformPlugin;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import jakarta.inject.Inject;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.teamcrimx.partyandfriends.api.database.MongoDatabaseImpl;
import net.teamcrimx.partyandfriends.api.friends.SimpleFriend;
import net.teamcrimx.partyandfriends.paper.inventories.FriendDetailInventory;
import net.teamcrimx.partyandfriends.paper.inventories.FriendInventory;
import net.teamcrimx.partyandfriends.paper.listener.InteractListener;
import net.teamcrimx.partyandfriends.paper.listener.PlayerJoinListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@PlatformPlugin(
        platform = "bukkit",
        name = "partyandfriends-paper",
        pluginFileNames = "plugin.yml",
        version = "{project.build.version}"
)
public class PaperPartyAndFriendsPlugin implements PlatformEntrypoint {

    private final Plugin plugin;
    private final PluginManager pluginManager;
    private PlayerManager playerManager;

    private final Map<UUID, SimpleFriend> simpleFriendMap = new HashMap<>();

    private final MiniMessage message = MiniMessage.builder().build();

    private FriendInventory friendInventory;
    private FriendDetailInventory friendDetailInventory;

    @Inject
    public PaperPartyAndFriendsPlugin(Plugin plugin, PluginManager pluginManager) {
        this.plugin = plugin;
        this.pluginManager = pluginManager;
    }

    @Override
    public void onLoad() {
        MongoDatabaseImpl.initializeDatabase();

        this.playerManager = ServiceRegistry.first(PlayerManager.class);

        this.registerListener();
        this.initializeInventories();
    }

    private void initializeInventories() {
        this.friendInventory = new FriendInventory(this);
        this.friendDetailInventory = new FriendDetailInventory(this);
    }

    private void registerListener() {
        this.pluginManager.registerEvents(new PlayerJoinListener(this), this.plugin);
        this.pluginManager.registerEvents(new InteractListener(this), this.plugin);
    }

    public void sendChannelMessageToNode(String channelName, String message, DataBuf dataBuf) {
        ChannelMessage.builder()
                .channel(channelName)
                .message(message)
                .buffer(dataBuf)
                .targetNodes()
                .build().send();
    }

    public Plugin plugin() {
        return plugin;
    }

    public PlayerManager playerManager() {
        return playerManager;
    }

    public Map<UUID, SimpleFriend> simpleFriendMap() {
        return simpleFriendMap;
    }

    public MiniMessage miniMessage() {
        return message;
    }

    public FriendInventory friendInventory() {
        return friendInventory;
    }

    public FriendDetailInventory friendDetailInventory() {
        return friendDetailInventory;
    }
}
