/**
 * VoteChain — Frontend Application Logic
 * Handles all API interactions, DOM updates, and user feedback.
 */

const API = 'https://blockchain-voting-app-q31v.onrender.com';
let selectedCandidateId = null;

// ===== INITIALIZATION =====
document.addEventListener('DOMContentLoaded', () => {
    initNavbar();
    initForms();
    loadCandidates();
    updateHeroStats();
});

// ===== NAVBAR =====
function initNavbar() {
    const navbar = document.getElementById('navbar');
    const toggle = document.getElementById('navToggle');
    const links = document.querySelector('.nav-links');

    // Scroll effect
    window.addEventListener('scroll', () => {
        navbar.classList.toggle('scrolled', window.scrollY > 50);
    });

    // Mobile toggle
    if (toggle) {
        toggle.addEventListener('click', () => {
            links.classList.toggle('open');
        });
    }

    // Active link on scroll
    const sections = document.querySelectorAll('section[id]');
    const navLinks = document.querySelectorAll('.nav-link');
    window.addEventListener('scroll', () => {
        let current = '';
        sections.forEach(sec => {
            const top = sec.offsetTop - 120;
            if (window.scrollY >= top) current = sec.getAttribute('id');
        });
        navLinks.forEach(link => {
            link.classList.toggle('active', link.getAttribute('href') === '#' + current);
        });
    });
}

// ===== FORMS =====
function initForms() {
    // Register form
    const registerForm = document.getElementById('registerForm');
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('regName').value.trim();
        const email = document.getElementById('regEmail').value.trim();

        if (!name || !email) {
            showToast('Please fill in all fields.', 'error');
            return;
        }

        const btn = document.getElementById('registerBtn');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Registering...';

        try {
            const res = await fetch(API + '/api/voters/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email })
            });
            const data = await res.json();

            const resultBox = document.getElementById('registerResult');
            resultBox.classList.remove('hidden', 'success', 'error');

            if (data.success) {
                resultBox.classList.add('success');
                resultBox.innerHTML = `
                    <strong>✅ Registration Successful!</strong><br>
                    Your Voter ID: <strong style="font-size:1.2rem;letter-spacing:0.05em;">${data.voterId}</strong><br>
                    <em style="font-size:0.8rem;">Save this ID — you'll need it to vote.</em>
                `;
                document.getElementById('voterId').value = data.voterId;
                registerForm.reset();
                showToast('Voter registered! Your ID: ' + data.voterId, 'success');
                updateHeroStats();
            } else {
                resultBox.classList.add('error');
                resultBox.innerHTML = `<strong>❌ Error:</strong> ${data.message}`;
                showToast(data.message, 'error');
            }
        } catch (err) {
            showToast('Network error. Is the server running?', 'error');
        }

        btn.disabled = false;
        btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></svg> Register Now`;
    });

    // Cast vote button
    document.getElementById('castVoteBtn').addEventListener('click', castVote);

    // Results button
    document.getElementById('refreshResultsBtn').addEventListener('click', loadResults);

    // Verify button
    document.getElementById('verifyChainBtn').addEventListener('click', verifyChain);

    // Explorer button
    document.getElementById('loadChainBtn').addEventListener('click', loadBlockchain);
}

// ===== LOAD CANDIDATES =====
async function loadCandidates() {
    try {
        const res = await fetch(API + '/api/candidates');
        const data = await res.json();
        if (!data.success) return;

        const grid = document.getElementById('candidatesGrid');
        grid.innerHTML = '';

        data.candidates.forEach(c => {
            const card = document.createElement('div');
            card.className = 'candidate-card';
            card.dataset.id = c.candidateId;
            card.innerHTML = `
                <div class="candidate-name">${c.name}</div>
                <div class="candidate-party">${c.party}</div>
                <div class="candidate-desc">${c.description}</div>
            `;
            card.addEventListener('click', () => selectCandidate(c.candidateId));
            grid.appendChild(card);
        });
    } catch (err) {
        console.error('Failed to load candidates:', err);
    }
}

function selectCandidate(id) {
    selectedCandidateId = id;
    document.querySelectorAll('.candidate-card').forEach(card => {
        card.classList.toggle('selected', card.dataset.id === id);
    });
    document.getElementById('castVoteBtn').disabled = false;
}

// ===== CAST VOTE =====
async function castVote() {
    const voterId = document.getElementById('voterId').value.trim();
    if (!voterId) {
        showToast('Please enter your Voter ID.', 'error');
        return;
    }
    if (!selectedCandidateId) {
        showToast('Please select a candidate.', 'error');
        return;
    }

    const btn = document.getElementById('castVoteBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Mining block...';

    try {
        const res = await fetch(API + '/api/vote', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ voterId, candidateId: selectedCandidateId })
        });
        const data = await res.json();

        const resultBox = document.getElementById('voteResult');
        resultBox.classList.remove('hidden', 'success', 'error');

        if (data.success) {
            resultBox.classList.add('success');
            resultBox.innerHTML = `
                <strong>✅ Vote Cast Successfully!</strong><br>
                Transaction ID: <code>${data.transactionId}</code><br>
                Block: <strong>#${data.blockIndex}</strong><br>
                Hash: <code style="font-size:0.75rem;word-break:break-all;">${data.transactionHash}</code>
            `;
            showToast('Vote mined into Block #' + data.blockIndex + '!', 'success');
            updateHeroStats();
        } else {
            resultBox.classList.add('error');
            resultBox.innerHTML = `<strong>❌ Error:</strong> ${data.message}`;
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Network error. Is the server running?', 'error');
    }

    btn.disabled = false;
    btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg> Cast Vote on Blockchain`;
}

// ===== LOAD RESULTS =====
async function loadResults() {
    try {
        const res = await fetch(API + '/api/results');
        const data = await res.json();
        if (!data.success) return;

        const container = document.getElementById('resultsContainer');
        container.innerHTML = '';

        const totalVotes = data.totalVotes || 0;

        data.results.forEach((r, i) => {
            const pct = totalVotes > 0 ? ((r.voteCount / totalVotes) * 100).toFixed(1) : 0;
            const card = document.createElement('div');
            card.className = 'result-card';
            card.innerHTML = `
                <div class="result-rank ${i === 0 && r.voteCount > 0 ? 'rank-1' : ''}">${i + 1}</div>
                <div class="result-info">
                    <div class="result-name">${r.candidateName}</div>
                    <div class="result-party">${r.party}</div>
                    <div class="result-bar-container">
                        <div class="result-bar" style="width: 0%;" data-width="${pct}%"></div>
                    </div>
                </div>
                <div class="result-votes">
                    <div class="result-vote-count">${r.voteCount}</div>
                    <div class="result-vote-label">${r.voteCount === 1 ? 'vote' : 'votes'} (${pct}%)</div>
                </div>
            `;
            container.appendChild(card);

            // Animate bar
            requestAnimationFrame(() => {
                setTimeout(() => {
                    card.querySelector('.result-bar').style.width = pct + '%';
                }, 100 + i * 150);
            });
        });

        if (totalVotes === 0) {
            container.innerHTML = '<p class="empty-state">No votes have been cast yet. Be the first to vote!</p>';
        }

        showToast('Results refreshed. ' + totalVotes + ' total votes.', 'info');
    } catch (err) {
        showToast('Failed to load results.', 'error');
    }
}

// ===== VERIFY CHAIN =====
async function verifyChain() {
    try {
        const res = await fetch(API + '/api/blockchain/verify');
        const data = await res.json();

        const resultBox = document.getElementById('verifyResult');
        resultBox.classList.remove('hidden', 'success', 'error', 'info');
        resultBox.classList.add(data.chainValid ? 'success' : 'error');
        resultBox.innerHTML = `<strong>${data.message}</strong>`;

        showToast(data.chainValid ? 'Chain integrity verified!' : 'Chain integrity FAILED!', data.chainValid ? 'success' : 'error');
    } catch (err) {
        showToast('Failed to verify chain.', 'error');
    }
}

// ===== LOAD BLOCKCHAIN =====
async function loadBlockchain() {
    try {
        const res = await fetch(API + '/api/blockchain');
        const data = await res.json();
        if (!data.success) return;

        const container = document.getElementById('chainContainer');
        container.innerHTML = '';

        // Reverse to show newest first
        const blocks = [...data.blocks].reverse();

        blocks.forEach((block, i) => {
            if (i > 0) {
                const link = document.createElement('div');
                link.className = 'chain-link';
                link.innerHTML = '⛓';
                container.appendChild(link);
            }

            const card = document.createElement('div');
            card.className = 'block-card';

            const time = new Date(block.timestamp).toLocaleString();
            const isGenesis = block.index === 0;

            let txHtml = '';
            if (block.transactions.length > 0) {
                txHtml = `
                    <div class="block-txs">
                        <div class="block-txs-title">Transactions (${block.transactionCount})</div>
                        ${block.transactions.map(tx => `
                            <div class="tx-item">
                                <div>Voter: <strong>${tx.voterId}</strong> → Candidate: <strong>${tx.candidateId}</strong></div>
                                <div class="tx-hash">TX: ${tx.hash}</div>
                            </div>
                        `).join('')}
                    </div>
                `;
            }

            card.innerHTML = `
                <div class="block-header">
                    <div class="block-index">${isGenesis ? '🏁 Genesis Block' : '⛏ Block #' + block.index}</div>
                    <div class="block-time">${time}</div>
                </div>
                <div class="block-hash-row">
                    <span class="hash-label">Hash</span>
                    <span class="hash-value">${block.hash}</span>
                </div>
                <div class="block-hash-row">
                    <span class="hash-label">Previous Hash</span>
                    <span class="hash-value">${block.previousHash}</span>
                </div>
                <div class="block-meta">
                    <span>🔢 Nonce: ${block.nonce}</span>
                    <span>📄 Transactions: ${block.transactionCount}</span>
                </div>
                ${txHtml}
            `;
            container.appendChild(card);
        });

        showToast(`Blockchain loaded. ${data.chainLength} blocks, difficulty: ${data.difficulty}`, 'info');
    } catch (err) {
        showToast('Failed to load blockchain.', 'error');
    }
}

// ===== HERO STATS =====
async function updateHeroStats() {
    try {
        const res = await fetch(API + '/api/blockchain');
        const data = await res.json();
        if (!data.success) return;

        animateValue('statBlocks', data.chainLength);
        const totalTx = data.blocks.reduce((sum, b) => sum + b.transactionCount, 0);
        animateValue('statVotes', totalTx);
        document.getElementById('statStatus').textContent = data.isValid ? '✓' : '✗';
    } catch (err) {
        // Silently fail on stats
    }
}

function animateValue(id, target) {
    const el = document.getElementById(id);
    const current = parseInt(el.textContent) || 0;
    if (current === target) return;
    const step = target > current ? 1 : -1;
    let val = current;
    const interval = setInterval(() => {
        val += step;
        el.textContent = val;
        if (val === target) clearInterval(interval);
    }, 50);
}

// ===== TOAST =====
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}
