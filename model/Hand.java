package bj.model;

import java.util.*;

/**
 * Represents a hand of playing cards in a blackjack game.
 * Provides methods for managing cards and calculating hand values.
 */
public class Hand {
    
    private final List<Card> cards = new ArrayList<>();
    
    /**
     * Clear all cards from the hand.
     */
    public void clear() {
        cards.clear();
    }
    
    /**
     * Add a card to the hand.
     * @param card The card to add
     */
    public void add(Card card) {
        cards.add(card);
    }
    
    /**
     * Get an unmodifiable view of the cards in this hand.
     * @return An unmodifiable list of cards
     */
    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
    
    /**
     * Calculate the total value of the hand with flexible Ace logic.
     * Aces count as 11 unless it would cause a bust, then they count as 1.
     * @return The optimal hand value
     */
    public int value() {
        int total = 0;
        int aces = 0;
        for (Card c : cards) {
            int v = c.getRank().getValue(); // ACE currently returns 11
            total += v;
            if (c.getRank() == bj.model.Rank.ACE) aces++;
        }
        // While bust and we still have aces counted as 11, convert one Ace from 11 to 1 (subtract 10)
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }
    
    /**
     * Check if the hand is "soft" (contains an Ace counted as 11).
     * A hand is soft if it has at least one Ace that can be counted as 11 without busting.
     * @return true if the hand is soft, false otherwise
     */
    public boolean isSoft() {
        int total = 0, aces = 0;
        for (Card c : cards) {
            int v = c.getRank().getValue();
            total += v;
            if (c.getRank() == bj.model.Rank.ACE) aces++;
        }
        // If after flexing we reduced at least one Ace (subtracting 10), the hand is NO longer soft.
        // A hand is "soft" iff there exists an Ace still counted as 11 after adjustment.
        while (total > 21 && aces > 0) { 
            total -= 10; 
            aces--; 
        }
        // If we still have any Ace counted as 11, it's soft
        return aces > 0;
    }
    
    /**
     * Check if the hand is bust (value > 21).
     * @return true if the hand value exceeds 21, false otherwise
     */
    public boolean isBust() {
        return value() > 21;
    }
    
    /**
     * Check if the hand is a blackjack (exactly two cards totaling 21).
     * @return true if the hand is a blackjack, false otherwise
     */
    public boolean isBlackjack() {
        // exactly two cards totaling 21 with Ace-flex already in value()
        return cards.size() == 2 && value() == 21;
    }
    
    /**
     * Get the number of cards in the hand.
     * @return The size of the hand
     */
    public int size() {
        return cards.size();
    }
    
    /**
     * String representation of the hand.
     * @return A string with all card representations joined by spaces
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(cards.get(i).toString());
        }
        return sb.toString();
    }
    
    /**
     * Get a localized string representation of the hand.
     * @return A string with all card localized names joined by spaces
     */
    public String getLocalizedString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(cards.get(i).getLocalizedName());
        }
        return sb.toString();
    }
}
