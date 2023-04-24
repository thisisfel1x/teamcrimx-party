package net.teamcrimx.partyandfriends.velocity;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.ext.platforminject.api.PlatformEntrypoint;
import eu.cloudnetservice.ext.platforminject.api.stereotype.PlatformPlugin;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.teamcrimx.partyandfriends.velocity.friends.command.FriendCommand;
import net.teamcrimx.partyandfriends.velocity.friends.command.FriendMsgCommand;
import net.teamcrimx.partyandfriends.velocity.party.command.PartyChatCommand;
import net.teamcrimx.partyandfriends.velocity.party.command.PartyCommand;
import org.checkerframework.checker.nullness.qual.NonNull;

@PlatformPlugin(
        platform = "velocity",
        name = "Velocity-Party",
        version = "{project.build.person}"
)
public class VelocityParty implements PlatformEntrypoint {

    private final ProxyServer proxyServer;

    private final PlayerManager playerManager = ServiceRegistry.first(PlayerManager.class);

    @jakarta.inject.Inject
    public VelocityParty(@NonNull ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Inject
    public void registerCommands(@NonNull CommandManager commandManager) {
        commandManager.register(commandManager.metaBuilder("friend").plugin(this).build(),
                new FriendCommand(this));
        commandManager.register(commandManager.metaBuilder("msg").plugin(this).build(),
                new FriendMsgCommand(this));

        // PARTY
        commandManager.register(commandManager.metaBuilder("party").plugin(this).build(),
                new PartyCommand(this));
        commandManager.register(commandManager.metaBuilder("p").plugin(this).build(),
                new PartyChatCommand(this));
    }

    public void sendChannelMessageToNode(String channelName, String message, DataBuf dataBuf) {
        ChannelMessage.builder()
                .channel(channelName)
                .message(message)
                .buffer(dataBuf)
                .targetNodes()
                .build().send();
    }

    public ProxyServer proxyServer() {
        return proxyServer;
    }

    public PlayerManager playerManager() {
        return playerManager;
    }
}
