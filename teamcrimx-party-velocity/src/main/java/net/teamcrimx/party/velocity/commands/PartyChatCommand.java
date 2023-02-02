package net.teamcrimx.party.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.teamcrimx.party.api.party.PartyConstants;
import net.teamcrimx.party.velocity.VelocityParty;

public class PartyChatCommand implements SimpleCommand {

    private final VelocityParty velocityParty;

    public PartyChatCommand(VelocityParty velocityParty) {
        this.velocityParty = velocityParty;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            return;
        }

        CloudPlayer cloudPlayer = CloudNetDriver.instance().serviceRegistry()
                .firstProvider(PlayerManager.class).onlinePlayer(player.getUniqueId());

        if(cloudPlayer == null) {
            return;
        }

        if(invocation.arguments().length == 0) {
            return;
        }

        ChannelMessage.builder()
                .channel(PartyConstants.PARTY_CHANNEL)
                .message(PartyConstants.PARTY_CHAT_MESSAGE)
                .buffer(DataBuf.empty().writeString(String.join(" ", invocation.arguments()))
                        .writeUniqueId(player.getUniqueId()))
                .targetNodes()
                .build().send();

    }
}
