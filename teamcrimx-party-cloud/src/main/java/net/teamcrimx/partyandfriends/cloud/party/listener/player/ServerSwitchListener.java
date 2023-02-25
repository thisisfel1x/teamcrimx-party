package net.teamcrimx.partyandfriends.cloud.party.listener.player;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.modules.bridge.event.BridgeProxyPlayerServerSwitchEvent;
import eu.cloudnetservice.modules.bridge.player.CloudPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.teamcrimx.partyandfriends.api.constants.ChatConstants;
import net.teamcrimx.partyandfriends.api.party.SimpleParty;
import net.teamcrimx.partyandfriends.cloud.PartyModule;

import java.util.UUID;

public class ServerSwitchListener {

    private final PartyModule partyModule;

    public ServerSwitchListener(PartyModule partyModule) {
        this.partyModule = partyModule;
    }

    @EventListener
    public void on(BridgeProxyPlayerServerSwitchEvent event) {
        CloudPlayer cloudPlayer = event.cloudPlayer();
        String targetServerName = event.target().serverName();

        if (!this.partyModule.getPartyManager().isInParty(cloudPlayer)) {
            return;
        }

        if (targetServerName.equalsIgnoreCase("Proxy")
                || targetServerName.equalsIgnoreCase("Node")) {
            return;
        }

        SimpleParty simpleParty = this.partyModule.getPartyManager().getPartyByCloudPlayer(cloudPlayer);
        if (simpleParty == null
                || !this.partyModule.getPartyManager().compareUUID(cloudPlayer.uniqueId(), simpleParty.partyLeader())) { // Perm check
            return;
        }

        for (UUID partyMember : simpleParty.partyMembers()) {
            if (cloudPlayer.uniqueId().toString().equalsIgnoreCase(partyMember.toString())) {
                continue;
            }

            CloudPlayer partyMemberCloudReference = this.partyModule.playerManager().onlinePlayer(partyMember);
            if (partyMemberCloudReference == null) {
                return;
            }

            if (partyMemberCloudReference.connectedService() == null) {
                partyMemberCloudReference.playerExecutor().sendChatMessage(ChatConstants.partyPrefix.append(Component
                        .text("Es ist ein Fehler aufgetreten, dich auf den Server deines Partyleaders zu senden", NamedTextColor.RED)));
                continue;
            }

            if (partyMemberCloudReference.connectedService().serverName().equalsIgnoreCase(targetServerName)) {
                continue;
            }

            partyMemberCloudReference.playerExecutor().connect(targetServerName);
            partyMemberCloudReference.playerExecutor().sendChatMessage(ChatConstants.partyPrefix.append(Component
                            .text("Deine Party betritt den Server "))
                    .append(Component.text(targetServerName, NamedTextColor.GREEN)));

        }
    }

}
