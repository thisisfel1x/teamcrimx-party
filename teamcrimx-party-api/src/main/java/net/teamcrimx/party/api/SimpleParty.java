package net.teamcrimx.party.api;

import java.util.List;
import java.util.UUID;

public class SimpleParty {

    private final UUID partyId;
    private UUID partyLeader;
    private final List<UUID> partyMembers;

    public SimpleParty(UUID partyId, UUID partyLeader, List<UUID> partyMembers) {
        this.partyId = partyId;
        this.partyLeader = partyLeader;
        this.partyMembers = partyMembers;
    }

    public UUID partyId() {
        return partyId;
    }

    public UUID partyLeader() {
        return partyLeader;
    }

    public void partyLeader(UUID partyLeader) {
        this.partyLeader = partyLeader;
    }

    public List<UUID> partyMembers() {
        return partyMembers;
    }

    @Override
    public String toString() {
        return "SimpleParty{" +
                "partyId=" + partyId +
                ", partyLeader=" + partyLeader +
                ", partyMembers=" + partyMembers +
                '}';
    }
}