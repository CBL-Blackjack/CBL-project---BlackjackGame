package bj.logic;

import bj.model.Deck;
import bj.model.Hand;
import bj.model.Card;

/**
 * Core game engine for blackjack logic.
 * Manages game state, betting, and card dealing operations.
 */
public class BlackjackEngine {
    
    private Deck deck;
    private final Hand player = new Hand();
    private final Hand dealer = new Hand();
    private int bankroll;
    private int currentBet;
    private GameState state = GameState.ROUND_START;
    private String lastOutcome = "";
    private BlackjackPayout payout = BlackjackPayout.THREE_TO_TWO;
    
    /**
     * Constructor for BlackjackEngine.
     * @param initialBankroll The starting bankroll amount
     */
    public BlackjackEngine(int initialBankroll) {
        this.bankroll = initialBankroll;
    }
    
    /**
     * Get the player's hand.
     * @return The player's hand
     */
    public Hand getPlayer() {
        return player;
    }
    
    /**
     * Get the dealer's hand.
     * @return The dealer's hand
     */
    public Hand getDealer() {
        return dealer;
    }
    
    /**
     * Get the current bankroll.
     * @return The current bankroll amount
     */
    public int getBankroll() {
        return bankroll;
    }
    
    /**
     * Get the current bet amount.
     * @return The current bet amount
     */
    public int getCurrentBet() {
        return currentBet;
    }
    
    /**
     * Get the current game state.
     * @return The current game state
     */
    public GameState getState() {
        return state;
    }
    
    /**
     * Set the bankroll amount.
     * @param amount The new bankroll amount
     */
    public void setBankroll(int amount) {
        this.bankroll = amount;
    }
    
    /**
     * Set the blackjack payout mode.
     * @param p The payout mode to set
     */
    public void setPayout(BlackjackPayout p) {
        this.payout = p;
    }
    
    /**
     * Get the current blackjack payout mode.
     * @return The current payout mode
     */
    public BlackjackPayout getPayout() {
        return payout;
    }
    
    /**
     * Place a bet for the current round.
     * @param amount The bet amount
     * @throws IllegalStateException if not in ROUND_START state
     * @throws IllegalArgumentException if amount is invalid
     */
    public void placeBet(int amount) {
        if (state != GameState.ROUND_START) {
            throw new IllegalStateException("Cannot place bet in state: " + state);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive, got: " + amount);
        }
        if (amount > bankroll) {
            throw new IllegalArgumentException("Insufficient bankroll. Available: " + bankroll + ", Requested: " + amount);
        }
        
        this.currentBet = amount;
        this.state = GameState.BET_PLACED;
    }
    
    /**
     * Start a new round by dealing initial cards.
     * @throws IllegalStateException if not in BET_PLACED state
     */
    public void startNewRound() {
        if (state != GameState.BET_PLACED) {
            throw new IllegalStateException("Cannot start new round in state: " + state);
        }
        if (currentBet == 0) {
            throw new IllegalStateException("Cannot start new round without placing a bet");
        }
        if (bankroll == 0) {
            throw new IllegalStateException("Cannot start new round with zero bankroll");
        }
        
        // Create new deck and shuffle
        deck = new Deck();
        
        // Clear both hands
        player.clear();
        dealer.clear();
        
        // Initial deal: player, dealer, player, dealer
        player.add(drawFromDeck());
        dealer.add(drawFromDeck());
        player.add(drawFromDeck());
        dealer.add(drawFromDeck());
        
        // Natural checks BEFORE handing turn to player
        boolean pBJ = player.isBlackjack();
        boolean dBJ = dealer.isBlackjack();
        
        if (pBJ || dBJ) {
            // round ends immediately; reveal dealer hole
            if (pBJ && dBJ) {
                lastOutcome = "Both have Blackjack. Push. Bet returned.";
                // bankroll unchanged
            } else if (pBJ) {
                int win = naturalWinAmount(currentBet);
                bankroll += win;
                lastOutcome = "Blackjack! You win $" + win + ".";
            } else { // dBJ only
                bankroll -= currentBet;
                lastOutcome = "Dealer Blackjack. You lose $" + currentBet + ".";
            }
            state = GameState.ROUND_END;
            return;
        }
        
        this.state = GameState.PLAYER_TURN;
    }
    
    /**
     * Reveal the dealer's hole card.
     * Currently a no-op, will be used in rendering step.
     */
    public void revealDealerHole() {
        // No-op for now, will be implemented in rendering step
    }
    
    /**
     * Reset the round while keeping the bankroll.
     * Clears hands, resets bet, clears outcome, and returns to ROUND_START state.
     */
    public void resetRoundKeepBankroll() {
        player.clear();
        dealer.clear();
        this.currentBet = 0;
        this.lastOutcome = "";
        this.state = GameState.ROUND_START;
    }
    
    /**
     * Draw a card from the deck.
     * @return The drawn card
     * @throws IllegalStateException if deck is null
     */
    public Card drawFromDeck() {
        if (deck == null) {
            throw new IllegalStateException("Deck not initialized");
        }
        
        // If deck is empty, rebuild a fresh deck automatically
        if (deck.isEmpty()) {
            deck = new Deck();
        }
        
        return deck.draw();
    }
    
    /**
     * Get the last round outcome message.
     * @return The last outcome message
     */
    public String getLastOutcome() {
        return lastOutcome;
    }
    
    /**
     * Check if the current round is over.
     * @return true if the round has ended
     */
    public boolean isRoundOver() {
        return state == GameState.ROUND_END;
    }
    
    // Helper methods
    
    /**
     * Calculate hand value (Ace flex logic to be added later).
     * @param h The hand to evaluate
     * @return The hand value
     */
    private int handValue(Hand h) {
        return h.value(); // Ace flex later (step 5)
    }
    
    /**
     * Check if a hand is bust (over 21).
     * @param h The hand to check
     * @return true if the hand is bust
     */
    private boolean isBust(Hand h) {
        return handValue(h) > 21;
    }
    
    /**
     * Compute win amount for natural blackjack based on payout mode.
     * @param bet The bet amount
     * @return The win amount
     */
    private int naturalWinAmount(int bet) {
        return switch (payout) {
            case THREE_TO_TWO -> (bet * 3) / 2; // integer floor for odd bets
            case SIX_TO_FIVE  -> (bet * 6) / 5; // optional alternative
        };
    }
    
    /**
     * Check if dealer hole card should be hidden based on game state.
     * @return true if dealer hole should be hidden
     */
    public boolean dealerHoleShouldBeHidden() {
        return state == GameState.PLAYER_TURN;
    }
    
    // Public round actions
    
    /**
     * Player hits (draws another card).
     */
    public void playerHit() {
        if (state != GameState.PLAYER_TURN) return;
        player.add(drawFromDeck());
        if (isBust(player)) {
            // Player busts → round ends, lose bet
            bankroll -= currentBet;
            lastOutcome = "Player busts (" + handValue(player) + "). You lose $" + currentBet + ".";
            state = GameState.ROUND_END;
        }
    }
    
    /**
     * Player stands (ends their turn).
     */
    public void playerStand() {
        if (state != GameState.PLAYER_TURN) return;
        state = GameState.DEALER_TURN;
        dealerPlayTo17();
        settleRound();
    }
    
    /**
     * Dealer plays until they have 17 or higher.
     */
    private void dealerPlayTo17() {
        // Dealer draws until value >= 17
        while (handValue(dealer) < 17) {
            dealer.add(drawFromDeck());
        }
    }
    
    /**
     * Settle the round and determine the outcome.
     */
    private void settleRound() {
        int pv = handValue(player);
        int dv = handValue(dealer);

        if (isBust(player)) {
            // already handled in playerHit(); keep for safety
            bankroll -= currentBet;
            lastOutcome = "Player busts (" + pv + "). You lose $" + currentBet + ".";
        } else if (isBust(dealer)) {
            bankroll += currentBet;
            lastOutcome = "Dealer busts (" + dv + "). You win $" + currentBet + "!";
        } else if (pv > dv) {
            bankroll += currentBet;
            lastOutcome = "You win $" + currentBet + "! (" + pv + " vs " + dv + ")";
        } else if (pv < dv) {
            bankroll -= currentBet;
            lastOutcome = "You lose $" + currentBet + ". (" + pv + " vs " + dv + ")";
        } else {
            lastOutcome = "Push (" + pv + " vs " + dv + "). Bet returned.";
            // bankroll unchanged
        }
        state = GameState.ROUND_END;
    }
}
