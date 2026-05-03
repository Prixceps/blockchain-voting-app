package com.votingchain.blockchain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The core blockchain data structure for the voting system.
 * Maintains an immutable, hash-linked chain of blocks containing vote transactions.
 * Thread-safe for concurrent vote submissions.
 */
public class Blockchain {

    private final List<Block> chain;
    private final List<VoteTransaction> pendingTransactions;
    private final int difficulty;

    /**
     * Creates a new blockchain with the specified mining difficulty.
     *
     * @param difficulty Number of leading zeros required in block hashes (proof-of-work)
     */
    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.difficulty = difficulty;
        // Create the genesis block
        createGenesisBlock();
    }

    /**
     * Creates the first block in the chain (genesis block).
     */
    private void createGenesisBlock() {
        Block genesis = new Block(0, "0", new ArrayList<>());
        genesis.mineBlock(difficulty);
        chain.add(genesis);
        System.out.println("Genesis block created.");
    }

    /**
     * Returns the most recent block in the chain.
     */
    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    /**
     * Adds a vote transaction to the pending queue.
     *
     * @param transaction The vote transaction to add
     */
    public synchronized void addTransaction(VoteTransaction transaction) {
        pendingTransactions.add(transaction);
    }

    /**
     * Mines all pending transactions into a new block and adds it to the chain.
     * This method is synchronized to prevent race conditions.
     */
    public synchronized void minePendingTransactions() {
        if (pendingTransactions.isEmpty()) {
            System.out.println("No pending transactions to mine.");
            return;
        }

        Block newBlock = new Block(
                chain.size(),
                getLatestBlock().getHash(),
                new ArrayList<>(pendingTransactions)
        );
        newBlock.mineBlock(difficulty);
        chain.add(newBlock);
        pendingTransactions.clear();

        System.out.println("New block mined and added to chain. Chain length: " + chain.size());
    }

    /**
     * Validates the entire blockchain by checking:
     * 1. Each block's hash is correctly computed
     * 2. Each block's previousHash matches the previous block's hash
     *
     * @return true if the blockchain is valid, false if tampering is detected
     */
    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // Verify the current block's hash
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Block #" + i + " hash is invalid!");
                return false;
            }

            // Verify the chain link
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                System.out.println("Block #" + i + " previous hash link is broken!");
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an unmodifiable view of the blockchain.
     */
    public List<Block> getChain() {
        return Collections.unmodifiableList(chain);
    }

    /**
     * Returns the current number of pending transactions.
     */
    public int getPendingCount() {
        return pendingTransactions.size();
    }

    /**
     * Returns the mining difficulty.
     */
    public int getDifficulty() {
        return difficulty;
    }
}
