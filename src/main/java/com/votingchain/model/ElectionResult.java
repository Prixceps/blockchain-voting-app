package com.votingchain.model;

/**
 * Aggregated election result for a single candidate.
 */
public class ElectionResult {

    private final String candidateId;
    private final String candidateName;
    private final String party;
    private int voteCount;

    public ElectionResult(String candidateId, String candidateName, String party) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.party = party;
        this.voteCount = 0;
    }

    public void incrementVoteCount() {
        this.voteCount++;
    }

    // --- Getters ---

    public String getCandidateId() {
        return candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public String getParty() {
        return party;
    }

    public int getVoteCount() {
        return voteCount;
    }
}
