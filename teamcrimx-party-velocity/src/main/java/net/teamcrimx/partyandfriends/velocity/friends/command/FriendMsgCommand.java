package net.teamcrimx.partyandfriends.velocity.friends.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.teamcrimx.partyandfriends.api.constants.ChatConstants;
import net.teamcrimx.partyandfriends.api.friends.FriendConstants;
import net.teamcrimx.partyandfriends.velocity.VelocityParty;

import java.util.Arrays;

public class FriendMsgCommand implements SimpleCommand {

    private final VelocityParty velocityParty;

    public FriendMsgCommand(VelocityParty velocityParty) {
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
            player.sendMessage(Component.join(JoinConfiguration.builder().build(), ChatConstants.friendPrefix,
                    Component.text("Bitte gebe einen Spielernamen an", NamedTextColor.RED)));
        } else {

            StringBuilder stringBuilder = new StringBuilder();
            Arrays.stream(invocation.arguments()).skip(1).forEach(string -> stringBuilder.append(string).append(" "));

            this.velocityParty.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_MSG_MESSAGE,
                    DataBuf.empty().writeUniqueId(cloudPlayer.uniqueId()).writeString(invocation.arguments()[0])
                            .writeString(stringBuilder.toString()));
        }
    }
}
