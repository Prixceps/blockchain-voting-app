package com.votingchain.model;

import java.util.UUID;

/**
 * Represents an election candidate.
 */
public class Candidate {

    private final String candidateId;
    private final String name;
    private final String party;
    private final String description;

    public Candidate(String name, String party, String description) {
        this.candidateId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.name = name;
        this.party = party;
        this.description = description;
    }

    // --- Getters ---

    public String getCandidateId() {
        return candidateId;
    }

    public String getName() {
        return name;
    }

    public String getParty() {
        return party;
    }

    public String getDescription() {
        return description;
    }
}
