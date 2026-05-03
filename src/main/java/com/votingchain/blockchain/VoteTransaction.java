package com.votingchain.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single vote transaction on the blockchain.
 * Each transaction is self-hashing for integrity verification.
 */
public class VoteTransaction {

    private final String transactionId;
    private final String voterId;
    private final String candidateId;
    private final long timestamp;
    private final String hash;

    public VoteTransaction(String voterId, String candidateId) {
        this.transactionId = UUID.randomUUID().toString();
        this.voterId = voterId;
        this.candidateId = candidateId;
        this.timestamp = Instant.now().toEpochMilli();
        this.hash = calculateHash();
    }

    /**
     * Calculates SHA-256 hash of this transaction's data.
     */
    private String calculateHash() {
        try {
            String data = transactionId + voterId + candidateId + timestamp;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating transaction hash", e);
        }
    }

    // --- Getters ---

    public String getTransactionId() {
        return transactionId;
    }

    public String getVoterId() {
        return voterId;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "VoteTransaction{" +
                "txId='" + transactionId + '\'' +
                ", voter='" + voterId + '\'' +
                ", candidate='" + candidateId + '\'' +
                ", hash='" + hash.substring(0, 16) + "...'" +
                '}';
    }
}
