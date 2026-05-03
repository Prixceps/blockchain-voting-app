package com.votingchain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a registered voter in the system.
 */
public class Voter {

    private final String voterId;
    private final String name;
    private final String email;
    private final long registrationTime;
    private boolean hasVoted;

    public Voter(String name, String email) {
        this.voterId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.name = name;
        this.email = email;
        this.registrationTime = Instant.now().toEpochMilli();
        this.hasVoted = false;
    }

    // --- Getters and Setters ---

    public String getVoterId() {
        return voterId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public long getRegistrationTime() {
        return registrationTime;
    }

    public boolean isHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }
}
