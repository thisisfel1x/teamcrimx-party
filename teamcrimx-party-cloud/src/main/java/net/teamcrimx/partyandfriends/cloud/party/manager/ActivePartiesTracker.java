package net.teamcrimx.partyandfriends.cloud.party.manager;

import net.teamcrimx.partyandfriends.api.party.SimpleParty;
import net.teamcrimx.partyandfriends.cloud.PartyAndFriendsModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActivePartiesTracker {

    private final PartyAndFriendsModule partyAndFriendsModule;
    private final Map<UUID, SimpleParty> activeParties;

    public ActivePartiesTracker(PartyAndFriendsModule partyAndFriendsModule) {
        this.partyAndFriendsModule = partyAndFriendsModule;
        this.activeParties = new HashMap<>();
    }

    public Map<UUID, SimpleParty> activeParties() {
        return activeParties;
    }

    public void checkActiveParties() {
        for (SimpleParty simpleParty : this.activeParties.values()) {
            if(simpleParty.partyMembers().size() > 1) {
                continue;
            }

            if (simpleParty.partyMembers().size() == 1) {
                if (System.currentTimeMillis() - simpleParty.creationDate() > (2 * 60 * 1000)) { // delete party after 2 minutes
                    for (UUID partyMember : simpleParty.partyMembers()) {
                        this.partyAndFriendsModule.getPartyManager()
                                .removeFromParty(partyMember);
                    }

                    this.activeParties.remove(simpleParty.partyId());
                    // TODO: delete party
                }
            } else if (simpleParty.partyMembers().size() == 0) {
                this.activeParties.remove(simpleParty.partyId());
            }
        }
    }
}
