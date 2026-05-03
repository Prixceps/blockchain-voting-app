package com.votingchain.controller;

import com.votingchain.blockchain.Block;
import com.votingchain.blockchain.VoteTransaction;
import com.votingchain.model.Candidate;
import com.votingchain.model.ElectionResult;
import com.votingchain.model.Voter;
import com.votingchain.service.VotingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller exposing all voting system endpoints.
 */
@RestController
@RequestMapping("/api")
public class VotingController {

    private final VotingService votingService;

    public VotingController(VotingService votingService) {
        this.votingService = votingService;
    }

    // ========== Voter Registration ==========

    /**
     * POST /api/voters/register
     * Body: { "name": "John Doe", "email": "john@example.com" }
     */
    @PostMapping("/voters/register")
    public ResponseEntity<Map<String, Object>> registerVoter(@RequestBody Map<String, String> body) {
        try {
            String name = body.get("name");
            String email = body.get("email");
            Voter voter = votingService.registerVoter(name, email);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Voter registered successfully!");
            response.put("voterId", voter.getVoterId());
            response.put("name", voter.getName());
            response.put("email", voter.getEmail());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== Vote Casting ==========

    /**
     * POST /api/vote
     * Body: { "voterId": "ABC12345", "candidateId": "XYZ67890" }
     */
    @PostMapping("/vote")
    public ResponseEntity<Map<String, Object>> castVote(@RequestBody Map<String, String> body) {
        try {
            String voterId = body.get("voterId");
            String candidateId = body.get("candidateId");
            VoteTransaction tx = votingService.castVote(voterId, candidateId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Vote cast and mined into blockchain successfully!");
            response.put("transactionId", tx.getTransactionId());
            response.put("transactionHash", tx.getHash());
            response.put("blockIndex", votingService.getBlockchain().getChain().size() - 1);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== Election Results ==========

    /**
     * GET /api/results
     */
    @GetMapping("/results")
    public ResponseEntity<Map<String, Object>> getResults() {
        List<ElectionResult> results = votingService.getResults();
        long totalVotes = votingService.getTotalVotesCast();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("totalVotes", totalVotes);
        response.put("results", results);
        response.put("chainValid", votingService.verifyChain());
        return ResponseEntity.ok(response);
    }

    // ========== Candidates ==========

    /**
     * GET /api/candidates
     */
    @GetMapping("/candidates")
    public ResponseEntity<Map<String, Object>> getCandidates() {
        Collection<Candidate> candidates = votingService.getCandidates();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("candidates", candidates);
        return ResponseEntity.ok(response);
    }

    // ========== Blockchain Explorer ==========

    /**
     * GET /api/blockchain
     */
    @GetMapping("/blockchain")
    public ResponseEntity<Map<String, Object>> getBlockchain() {
        List<Block> chain = votingService.getBlockchain().getChain();

        // Convert blocks to serializable maps
        List<Map<String, Object>> blockList = new ArrayList<>();
        for (Block block : chain) {
            Map<String, Object> blockMap = new LinkedHashMap<>();
            blockMap.put("index", block.getIndex());
            blockMap.put("timestamp", block.getTimestamp());
            blockMap.put("previousHash", block.getPreviousHash());
            blockMap.put("hash", block.getHash());
            blockMap.put("nonce", block.getNonce());
            blockMap.put("transactionCount", block.getTransactions().size());

            List<Map<String, Object>> txList = new ArrayList<>();
            for (VoteTransaction tx : block.getTransactions()) {
                Map<String, Object> txMap = new LinkedHashMap<>();
                txMap.put("transactionId", tx.getTransactionId());
                txMap.put("voterId", tx.getVoterId());
                txMap.put("candidateId", tx.getCandidateId());
                txMap.put("timestamp", tx.getTimestamp());
                txMap.put("hash", tx.getHash());
                txList.add(txMap);
            }
            blockMap.put("transactions", txList);
            blockList.add(blockMap);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("chainLength", chain.size());
        response.put("difficulty", votingService.getBlockchain().getDifficulty());
        response.put("isValid", votingService.verifyChain());
        response.put("blocks", blockList);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/blockchain/verify
     */
    @GetMapping("/blockchain/verify")
    public ResponseEntity<Map<String, Object>> verifyBlockchain() {
        boolean isValid = votingService.verifyChain();
        int chainLength = votingService.getBlockchain().getChain().size();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("chainValid", isValid);
        response.put("chainLength", chainLength);
        response.put("message", isValid
                ? "✅ Blockchain integrity verified. All " + chainLength + " blocks are valid."
                : "❌ Blockchain integrity check FAILED. Tampering detected!");
        return ResponseEntity.ok(response);
    }
}
