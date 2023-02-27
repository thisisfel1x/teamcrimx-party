package net.teamcrimx.partyandfriends.velocity.party.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.teamcrimx.partyandfriends.api.party.PartyConstants;
import net.teamcrimx.partyandfriends.velocity.VelocityParty;

import java.util.Arrays;
import java.util.List;

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

        if (cloudPlayer == null) {
            return;
        }

        if (invocation.arguments().length == 0) {
            return;
        }

        this.velocityParty.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL, PartyConstants.PARTY_CHAT_MESSAGE,
                DataBuf.empty().writeUniqueId(player.getUniqueId()).writeString(String.join(" ", invocation.arguments())));
    }
}
