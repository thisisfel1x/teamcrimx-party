package net.teamcrimx.party.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.module.driver.DriverModule;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.teamcrimx.party.api.PartyConstants;
import net.teamcrimx.party.velocity.VelocityParty;

import java.util.List;
import java.util.stream.Collectors;

// TODO: remove redundancy in switch block
public class PartyCommand implements SimpleCommand {

    private final VelocityParty velocityParty;

    public PartyCommand(VelocityParty velocityParty) {
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
            player.sendMessage(Component.text("Zu wenig Argumente"));
            return;
        }

        if(invocation.arguments().length == 1) {
            switch (invocation.arguments()[0].toLowerCase()) {
                case "deleteproperties" -> {
                    this.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL, "delete",
                            DataBuf.empty().writeUniqueId(player.getUniqueId()));
                }
                case "create" -> {
                    this.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL, PartyConstants.PARTY_CREATION_MESSAGE,
                            DataBuf.empty().writeUniqueId(player.getUniqueId()));
                }
                case "leave" -> {
                    this.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL, PartyConstants.PARTY_LEAVE_MESSAGE,
                            DataBuf.empty().writeUniqueId(player.getUniqueId()));
                }
                case "list" -> {
                    this.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL, PartyConstants.PARTY_LIST_MESSAGE,
                            DataBuf.empty().writeUniqueId(player.getUniqueId()));
                }
                case "promote", "kick", "invite", "accept" -> player.sendMessage(Component.text("bitte gebe einen spielernamen an!"));
                case "close" -> {
                    this.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL, PartyConstants.PARTY_CLOSE_MESSAGE,
                            DataBuf.empty().writeUniqueId(player.getUniqueId()));
                }
            }
        } else if(invocation.arguments().length == 2) {
            switch (invocation.arguments()[0].toLowerCase()) { // switch first argument for sub-arg-check
                case "promote" -> {
                    String playerNameToPromote = invocation.arguments()[1]; // not valid yet TODO: validation check - cloud side done
                    player.sendMessage(Component.text("versuche " + playerNameToPromote + " zu promoten"));
                    this.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL, PartyConstants.PARTY_PROMOTE_MESSAGE,
                            DataBuf.empty().writeString(playerNameToPromote).writeUniqueId(player.getUniqueId()));
                }
                case "kick" -> {
                    String playerNameToKick = invocation.arguments()[1]; // not valid yet
                    player.sendMessage(Component.text("versuche " + playerNameToKick + " zu kicken"));
                    this.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL, PartyConstants.PARTY_KICK_MESSAGE,
                            DataBuf.empty().writeString(playerNameToKick).writeUniqueId(player.getUniqueId()));
                }
                case "invite" -> {
                    String playerNameToInvite = invocation.arguments()[1]; // not valid yet TODO: validation check - cloud side done
                    player.sendMessage(Component.text("versuche " + playerNameToInvite + " einzuladen"));
                    this.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL, PartyConstants.PARTY_INVITE_MESSAGE,
                            DataBuf.empty().writeString(playerNameToInvite).writeUniqueId(player.getUniqueId()));
                }
                case "accept" -> {
                    String playerNameToJoin = invocation.arguments()[1]; // not valid yet TODO: validation check - cloud side done
                    player.sendMessage(Component.text("versuche " + playerNameToJoin + " party zu joinen"));
                    this.sendChannelMessageToNode(PartyConstants.PARTY_CHANNEL, PartyConstants.PARTY_JOIN_MESSAGE,
                            DataBuf.empty().writeString(playerNameToJoin).writeUniqueId(player.getUniqueId()));
                }
            }

        }

    }

    public void sendChannelMessageToNode(String channelName, String message, DataBuf dataBuf) {
        ChannelMessage.builder()
                .channel(channelName)
                .message(message)
                .buffer(dataBuf)
                .targetNodes()
                .build().send();
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        switch (invocation.arguments().length) {
            case 1 -> {
                return List.of("create", "invite", "accept", "leave", "kick", "promote", "close", "list", "deleteproperties");
            }
            case 2 -> {
                switch (invocation.arguments()[0].toLowerCase()) {
                    case "invite" -> {
                        return this.velocityParty.proxyServer().getAllPlayers()
                                .stream().map(Player::getUsername).collect(Collectors.toList()); // TODO: get only users from current server
                    }
                    case "kick" -> {
                        // TODO: get response of current party members
                    }
                }
            }
        }
        return List.of();
    }

}
