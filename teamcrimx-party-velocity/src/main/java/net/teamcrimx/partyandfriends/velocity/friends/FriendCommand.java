package net.teamcrimx.partyandfriends.velocity.friends;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.teamcrimx.partyandfriends.api.constants.ChatConstants;
import net.teamcrimx.partyandfriends.api.friends.FriendConstants;
import net.teamcrimx.partyandfriends.velocity.VelocityParty;

public class FriendCommand implements SimpleCommand {

    private final VelocityParty velocityParty;

    public FriendCommand(VelocityParty velocityParty) {
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
            player.sendMessage(ChatConstants.friendHelpMessage);
            return;
        }

        if (invocation.arguments().length == 1) {
            switch (invocation.arguments()[0].toLowerCase()) {
                case "list" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_LIST_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()));
                case "denyall" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_DENY_ALL_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()));
                case "acceptall" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_ACCEPT_ALL_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()));
                case "removeall" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_REMOVE_ALL_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()));
                case "listrequests" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_LIST_REQUESTS_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()));
                default -> player.sendMessage(ChatConstants.friendPrefix.append(Component.text("Bitte gebe einen Spielernamen an",
                        NamedTextColor.RED)));
            }
        } else if(invocation.arguments().length == 2) {
            String playerName = invocation.arguments()[1];
            switch (invocation.arguments()[0].toLowerCase()) {
                case "add" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_ADD_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()).writeString(playerName));
                case "accept" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_ACCEPT_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()).writeString(playerName));
                case "remove" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_REMOVE_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()).writeString(playerName));
                case "deny" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_DENY_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()).writeString(playerName));
                case "jump" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_JUMP_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()).writeString(playerName));
                case "msg" -> this.sendChannelMessageToNode(FriendConstants.FRIEND_CHANNEL, FriendConstants.FRIEND_MSG_MESSAGE,
                        DataBuf.empty().writeUniqueId(player.getUniqueId()).writeString(playerName));
            }
        }

    }

    public void sendChannelMessageToNode(String channelName, String message, DataBuf dataBuf) {
        this.velocityParty.sendChannelMessageToNode(channelName, message, dataBuf);
    }
}
