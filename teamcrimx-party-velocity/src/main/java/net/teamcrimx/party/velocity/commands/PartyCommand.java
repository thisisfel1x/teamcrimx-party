package net.teamcrimx.party.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import eu.cloudnetservice.common.document.gson.JsonDocument;
import eu.cloudnetservice.driver.CloudNetDriver;
import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.teamcrimx.party.api.PartyConstants;
import net.teamcrimx.party.velocity.VelocityParty;

import java.util.List;
import java.util.UUID;

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
                case "create":
                    if(cloudPlayer.properties().contains(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY)
                            && cloudPlayer.properties().getBoolean(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY)) {
                        player.sendMessage(Component.text("du bist bereits in einer party"));
                    } else {
                        ChannelMessage.builder()
                                .channel(PartyConstants.PARTY_CHANNEL)
                                .message(PartyConstants.PARTY_CREATION_MESSAGE)
                                .buffer(DataBuf.empty().writeUniqueId(player.getUniqueId()))
                                .targetNodes()
                                .build().send();
                    }
                case "leave":
                    if(!cloudPlayer.properties().contains(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY)
                            && !cloudPlayer.properties().getBoolean(PartyConstants.HAS_PARTY_DOCUMENT_PROPERTY)) {
                        player.sendMessage(Component.text("du bist in keiner party"));
                    } else {
                        ChannelMessage.builder()
                                .channel(PartyConstants.PARTY_CHANNEL)
                                .message(PartyConstants.PARTY_LEAVE_MESSAGE)
                                .buffer(DataBuf.empty().writeUniqueId(player.getUniqueId()))
                                .targetNodes()
                                .build().send();
                    }
                case "promote", "kick", "invite", "accept":
                    player.sendMessage(Component.text("bitte gebe einen spielernamen an!"));
                case "close":
                    ChannelMessage.builder()
                            .channel(PartyConstants.PARTY_CHANNEL)
                            .message(PartyConstants.PARTY_CLOSE_MESSAGE)
                            .buffer(DataBuf.empty().writeUniqueId(player.getUniqueId()))
                            .targetNodes()
                            .build().send();
            }
        } else if(invocation.arguments().length == 2) {
            switch (invocation.arguments()[0].toLowerCase()) { // switch first argument for sub-arg-check
                case "promote":
                    String playerNameToPromote = invocation.arguments()[1]; // not valid yet TODO: validation check - cloud side done
                    player.sendMessage(Component.text("versuche " + playerNameToPromote + " zu promoten"));
                    ChannelMessage.builder()
                            .channel(PartyConstants.PARTY_CHANNEL)
                            .message(PartyConstants.PARTY_PROMOTE_MESSAGE)
                            .buffer(DataBuf.empty().writeString(playerNameToPromote))
                            .targetNodes()
                            .build().send();
                case "kick":
                    String playerNameToKick = invocation.arguments()[1]; // not valid yet
                    player.sendMessage(Component.text("versuche " + playerNameToKick + " zu kicken"));
                    ChannelMessage.builder()
                            .channel(PartyConstants.PARTY_CHANNEL)
                            .message(PartyConstants.PARTY_PROMOTE_MESSAGE)
                            .buffer(DataBuf.empty().writeString(playerNameToKick))
                            .targetNodes()
                            .build().send();
            }

        }

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of("create", "invite", "join", "leave", "kick", "promote", "close");
    }

}
