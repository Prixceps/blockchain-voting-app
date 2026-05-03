package com.votingchain.blockchain;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single block in the voting blockchain.
 * Contains vote transactions, cryptographic hashes, and proof-of-work nonce.
 */
public class Block {

    private final int index;
    private final long timestamp;
    private final String previousHash;
    private final List<VoteTransaction> transactions;
    private int nonce;
    private String hash;

    private static final Gson GSON = new Gson();

    public Block(int index, String previousHash, List<VoteTransaction> transactions) {
        this.index = index;
        this.timestamp = Instant.now().toEpochMilli();
        this.previousHash = previousHash;
        this.transactions = new ArrayList<>(transactions);
        this.nonce = 0;
        this.hash = calculateHash();
    }

    /**
     * Calculates SHA-256 hash of the block's contents.
     */
    public String calculateHash() {
        try {
            String data = index + timestamp + previousHash + GSON.toJson(transactions) + nonce;
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
            throw new RuntimeException("Error calculating block hash", e);
        }
    }

    /**
     * Mines the block by finding a nonce that produces a hash with the required
     * number of leading zeros (proof-of-work).
     *
     * @param difficulty Number of leading zeros required in the hash
     */
    public void mineBlock(int difficulty) {
        String target = "0".repeat(difficulty);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block #" + index + " mined: " + hash);
    }

    // --- Getters ---

    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public int getNonce() {
        return nonce;
    }

    public List<VoteTransaction> getTransactions() {
        return new ArrayList<>(transactions);
    }
}
