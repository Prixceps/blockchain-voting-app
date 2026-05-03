package com.votingchain.service;

import com.votingchain.blockchain.Block;
import com.votingchain.blockchain.Blockchain;
import com.votingchain.blockchain.VoteTransaction;
import com.votingchain.model.Candidate;
import com.votingchain.model.ElectionResult;
import com.votingchain.model.Voter;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core service handling all voting operations.
 * Manages voter registration, vote casting, blockchain operations, and result tallying.
 */
@Service
public class VotingService {

    private final Blockchain blockchain;
    private final Map<String, Voter> voterRegistry;
    private final Map<String, Candidate> candidateRegistry;

    // Mining difficulty: 4 leading zeros (~100ms per block on modern hardware)
    private static final int MINING_DIFFICULTY = 4;

    public VotingService() {
        this.blockchain = new Blockchain(MINING_DIFFICULTY);
        this.voterRegistry = new ConcurrentHashMap<>();
        this.candidateRegistry = new ConcurrentHashMap<>();
    }

    /**
     * Initializes default candidates for the election.
     */
    @PostConstruct
    public void initCandidates() {
        addCandidate("Alice Johnson", "Progressive Party",
                "Experienced leader with 15 years in public service. Focused on education reform and renewable energy.");
        addCandidate("Bob Williams", "Conservative Alliance",
                "Business leader and former governor. Champions fiscal responsibility and national security.");
        addCandidate("Carol Martinez", "Unity Coalition",
                "Community organizer and civil rights advocate. Prioritizes healthcare access and social justice.");
        addCandidate("David Chen", "Innovation Party",
                "Tech entrepreneur and futurist. Advocates for digital infrastructure and AI governance.");
        System.out.println("Default candidates initialized: " + candidateRegistry.size());
    }

    /**
     * Adds a candidate to the election.
     */
    private void addCandidate(String name, String party, String description) {
        Candidate candidate = new Candidate(name, party, description);
        candidateRegistry.put(candidate.getCandidateId(), candidate);
    }

    /**
     * Registers a new voter.
     *
     * @param name  Voter's full name
     * @param email Voter's email address
     * @return The registered Voter object
     * @throws IllegalArgumentException if email is already registered
     */
    public Voter registerVoter(String name, String email) {
        // Check for duplicate email
        boolean emailExists = voterRegistry.values().stream()
                .anyMatch(v -> v.getEmail().equalsIgnoreCase(email));
        if (emailExists) {
            throw new IllegalArgumentException("A voter with this email is already registered.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Voter name is required.");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }

        Voter voter = new Voter(name.trim(), email.trim().toLowerCase());
        voterRegistry.put(voter.getVoterId(), voter);
        System.out.println("Voter registered: " + voter.getVoterId() + " (" + voter.getName() + ")");
        return voter;
    }

    /**
     * Casts a vote by creating a transaction and mining it into the blockchain.
     *
     * @param voterId     The voter's unique ID
     * @param candidateId The candidate's unique ID
     * @return The created VoteTransaction
     * @throws IllegalArgumentException if voter/candidate not found or voter already voted
     */
    public VoteTransaction castVote(String voterId, String candidateId) {
        // Validate voter
        Voter voter = voterRegistry.get(voterId);
        if (voter == null) {
            throw new IllegalArgumentException("Voter ID not found. Please register first.");
        }
        if (voter.isHasVoted()) {
            throw new IllegalArgumentException("This voter has already cast their vote. Double voting is not allowed.");
        }

        // Validate candidate
        Candidate candidate = candidateRegistry.get(candidateId);
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate ID not found.");
        }

        // Create transaction and mine
        VoteTransaction transaction = new VoteTransaction(voterId, candidateId);
        blockchain.addTransaction(transaction);
        blockchain.minePendingTransactions();

        // Mark voter as having voted
        voter.setHasVoted(true);

        System.out.println("Vote cast: " + voter.getName() + " → " + candidate.getName());
        return transaction;
    }

    /**
     * Tallies all votes from the blockchain and returns results per candidate.
     *
     * @return List of ElectionResult sorted by vote count (descending)
     */
    public List<ElectionResult> getResults() {
        // Initialize results for all candidates
        Map<String, ElectionResult> resultMap = new LinkedHashMap<>();
        for (Candidate candidate : candidateRegistry.values()) {
            resultMap.put(candidate.getCandidateId(),
                    new ElectionResult(candidate.getCandidateId(), candidate.getName(), candidate.getParty()));
        }

        // Tally votes from blockchain (skip genesis block)
        for (Block block : blockchain.getChain()) {
            for (VoteTransaction tx : block.getTransactions()) {
                ElectionResult result = resultMap.get(tx.getCandidateId());
                if (result != null) {
                    result.incrementVoteCount();
                }
            }
        }

        // Sort by vote count descending
        List<ElectionResult> results = new ArrayList<>(resultMap.values());
        results.sort((a, b) -> Integer.compare(b.getVoteCount(), a.getVoteCount()));
        return results;
    }

    /**
     * Verifies the integrity of the entire blockchain.
     *
     * @return true if the chain is valid and untampered
     */
    public boolean verifyChain() {
        return blockchain.isChainValid();
    }

    /**
     * Returns the full blockchain for transparency/auditing.
     */
    public Blockchain getBlockchain() {
        return blockchain;
    }

    /**
     * Returns all registered candidates.
     */
    public Collection<Candidate> getCandidates() {
        return candidateRegistry.values();
    }

    /**
     * Returns all registered voters.
     */
    public Collection<Voter> getVoters() {
        return voterRegistry.values();
    }

    /**
     * Returns total number of votes cast.
     */
    public long getTotalVotesCast() {
        return voterRegistry.values().stream().filter(Voter::isHasVoted).count();
    }
}
