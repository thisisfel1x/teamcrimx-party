package net.teamcrimx.party.cloud.listener;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import net.teamcrimx.party.api.party.PartyConstants;
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
                this.partyModule.getPartyManager()
                        .parsePlayerNameAndExecute(event.content(), PartyConstants.PARTY_INVITE_MESSAGE);
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_JOIN_MESSAGE)) {
                this.partyModule.getPartyManager()
                        .parsePlayerNameAndExecute(event.content(), PartyConstants.PARTY_JOIN_MESSAGE);
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_LEAVE_MESSAGE)) {
                this.partyModule.getPartyManager().leaveParty(event.content());
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_PROMOTE_MESSAGE)) {
                this.partyModule.getPartyManager()
                        .parsePlayerNameAndExecute(event.content(), PartyConstants.PARTY_PROMOTE_MESSAGE);
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_KICK_MESSAGE)) {
                this.partyModule.getPartyManager()
                        .parsePlayerNameAndExecute(event.content(), PartyConstants.PARTY_KICK_MESSAGE);
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_CLOSE_MESSAGE)) {
                this.partyModule.getPartyManager().closeParty(event.content());
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_LIST_MESSAGE)) {
                this.partyModule.getPartyManager().sendPartyListMessage(event.content());
            } else if(event.message().equalsIgnoreCase("delete")) { // TODO: DEVELOPMENT ONLY
                this.partyModule.getPartyManager().delete(event.content());
            } else if(event.message().equalsIgnoreCase(PartyConstants.PARTY_CHAT_MESSAGE)) {
                this.partyModule.getPartyManager().chat(event.content());
            }
        }
    }

}
