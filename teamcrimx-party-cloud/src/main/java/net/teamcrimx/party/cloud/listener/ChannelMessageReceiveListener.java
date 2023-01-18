package net.teamcrimx.party.cloud.listener;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import net.teamcrimx.party.api.PartyConstants;
import net.teamcrimx.party.cloud.PartyModule;

public class ChannelMessageReceiveListener {

    private final PartyModule partyModule;

    public ChannelMessageReceiveListener(PartyModule partyModule) {
        this.partyModule = partyModule;
    }

    @EventListener
    public void on(ChannelMessageReceiveEvent event) {
        if(event.channel().equalsIgnoreCase(PartyConstants.PARTY_CHANNEL)) {
            if(event.message().equalsIgnoreCase(PartyConstants.PARTY_CREATION_MESSAGE)) {
                this.partyModule.getPartyManager().createParty(event.content());
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_INVITE_MESSAGE)) {
                // TODO: invite player with invitation expiration
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_JOIN_MESSAGE)) {
                // TODO: add player to party
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_LEAVE_MESSAGE)) {
                // TODO: remove player from party
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_PROMOTE_MESSAGE)) {
                // TODO: promote player x as party leader
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_KICK_MESSAGE)) {
                // TODO: kick player x from party
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_CLOSE_MESSAGE)) {
                // TODO: delete party
            }
        }
    }

}
