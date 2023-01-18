package net.teamcrimx.party.api;

import java.util.List;
import java.util.UUID;

public record SimpleParty(UUID partyId, UUID partyLeader, List<UUID> partyMembers) {
}
