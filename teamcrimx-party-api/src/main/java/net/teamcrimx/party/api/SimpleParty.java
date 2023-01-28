package net.teamcrimx.party.api;

import java.util.List;
import java.util.UUID;

public class SimpleParty {

    private final UUID partyId;
    private UUID partyLeader;
    private final List<UUID> partyMembers;
    private final long creationDate;

    public SimpleParty(UUID partyId, UUID partyLeader, List<UUID> partyMembers, long creationDate) {
        this.partyId = partyId;
        this.partyLeader = partyLeader;
        this.partyMembers = partyMembers;
        this.creationDate = creationDate;
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

    public long creationDate() {
        return creationDate;
    }

    @Override
    public String toString() {
        return "SimpleParty{" +
                "partyId=" + partyId +
                ", partyLeader=" + partyLeader +
                ", partyMembers=" + partyMembers +
                ", creationDate=" + creationDate +
                '}';
    }
}