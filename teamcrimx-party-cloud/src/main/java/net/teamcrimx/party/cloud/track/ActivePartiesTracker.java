package net.teamcrimx.party.cloud.track;

import net.teamcrimx.party.api.SimpleParty;
import net.teamcrimx.party.cloud.PartyModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActivePartiesTracker {

    private final PartyModule partyModule;
    private final Map<UUID, SimpleParty> activeParties;

    public ActivePartiesTracker(PartyModule partyModule) {
        this.partyModule = partyModule;
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
            if(System.currentTimeMillis() - simpleParty.creationDate() > (2 * 60 * 1000)) { // delete party after x seconds
                for (UUID partyMember : simpleParty.partyMembers()) {
                    this.partyModule.getPartyManager()
                            .removeFromPartyIfIn(this.partyModule.getPartyManager().getCloudPlayerById(partyMember));
                }
                // TODO: delete party
            }
        }
    }
}
