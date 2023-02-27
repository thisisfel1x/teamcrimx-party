package net.teamcrimx.partyandfriends.cloud.party.listener;

import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import net.teamcrimx.partyandfriends.api.party.PartyConstants;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;

public class ChannelMessageReceiveListener {

    private final PartyAndFriendsModule partyAndFriendsModule;

    public ChannelMessageReceiveListener(PartyAndFriendsModule partyAndFriendsModule) {
        this.partyAndFriendsModule = partyAndFriendsModule;
    }

    @EventListener
    public void on(ChannelMessageReceiveEvent event) {
        if (event.channel().equalsIgnoreCase(PartyConstants.PARTY_CHANNEL)) {
            if (event.message().equalsIgnoreCase(PartyConstants.PARTY_CREATION_MESSAGE)) {
                this.partyAndFriendsModule.getPartyManager().createParty(event.content());
            } else if (event.message().equalsIgnoreCase(PartyConstants.PARTY_INVITE_MESSAGE)) {
                this.partyAndFriendsModule.getPartyManager()
                        .parsePlayerNameAndExecute(event.content(), PartyConstants.PARTY_INVITE_MESSAGE);
            } else if (event.message().equalsIgnoreCase(PartyConstants.PARTY_JOIN_MESSAGE)) {
                this.partyAndFriendsModule.getPartyManager()
                        .parsePlayerNameAndExecute(event.content(), PartyConstants.PARTY_JOIN_MESSAGE);
            } else if (event.message().equalsIgnoreCase(PartyConstants.PARTY_LEAVE_MESSAGE)) {
                this.partyAndFriendsModule.getPartyManager().removeFromParty(event.content().readUniqueId());
            } else if (event.message().equalsIgnoreCase(PartyConstants.PARTY_PROMOTE_MESSAGE)) {
                this.partyAndFriendsModule.getPartyManager()
                        .parsePlayerNameAndExecute(event.content(), PartyConstants.PARTY_PROMOTE_MESSAGE);
            } else if (event.message().equalsIgnoreCase(PartyConstants.PARTY_KICK_MESSAGE)) {
                this.partyAndFriendsModule.getPartyManager()
                        .parsePlayerNameAndExecute(event.content(), PartyConstants.PARTY_KICK_MESSAGE);
            } else if (event.message().equalsIgnoreCase(PartyConstants.PARTY_CLOSE_MESSAGE)) {
                this.partyAndFriendsModule.getPartyManager().closeParty(event.content().readUniqueId());
            } else if (event.message().equalsIgnoreCase(PartyConstants.PARTY_LIST_MESSAGE)) {
                this.partyAndFriendsModule.getPartyManager().sendPartyListMessage(event.content());
            } else if (event.message().equalsIgnoreCase("delete")) { // TODO: DEVELOPMENT ONLY
                this.partyAndFriendsModule.getPartyManager().delete(event.content().readUniqueId());
            } else if (event.message().equalsIgnoreCase(PartyConstants.PARTY_CHAT_MESSAGE)) {
                this.partyAndFriendsModule.getPartyManager().chat(event.content());
            }
        }
    }

}
