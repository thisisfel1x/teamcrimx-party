package net.teamcrimx.party.cloud.track;

import net.teamcrimx.party.api.SimpleParty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActivePartiesTracker {

    private final Map<UUID, SimpleParty> activeParties;

    public ActivePartiesTracker() {
        this.activeParties = new HashMap<>();
    }

    public Map<UUID, SimpleParty> activeParties() {
        return activeParties;
    }
}
