package net.teamcrimx.partyandfriends.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.teamcrimx.partyandfriends.velocity.friends.FriendCommand;
import net.teamcrimx.partyandfriends.velocity.party.commands.PartyChatCommand;
import net.teamcrimx.partyandfriends.velocity.party.commands.PartyCommand;
import org.slf4j.Logger;

@Plugin(id = "teamcrimx-party", name = "teamcrimx-party", version = "ALPHA 1",
        url = "teamcrimx.net", authors = {"fel1x"})
public class VelocityParty {

    private final ProxyServer proxyServer;
    private final Logger logger;

    private final PlayerManager playerManager = CloudNetDriver.instance().serviceRegistry()
            .firstProvider(PlayerManager.class);

    @Inject
    public VelocityParty(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void on(ProxyInitializeEvent event) {
        CommandManager commandManager = this.proxyServer.getCommandManager();
        // FRIENDS
        commandManager.register(commandManager.metaBuilder("friend").plugin(this).build(),
                new FriendCommand(this));

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
