package bj.model;

import java.util.Objects;

/**
 * Represents a playing card with a suit and rank.
 * Immutable class that ensures card uniqueness based on suit and rank combination.
 */
public class Card {
    
    private final Suit suit;
    private final Rank rank;
    
    /**
     * Constructor for Card.
     * @param suit The suit of the card
     * @param rank The rank of the card
     */
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }
    
    /**
     * Get the suit of this card.
     * @return The suit of the card
     */
    public Suit getSuit() {
        return suit;
    }
    
    /**
     * Get the rank of this card.
     * @return The rank of the card
     */
    public Rank getRank() {
        return rank;
    }
    
    /**
     * Get the numeric value of this card.
     * @return The blackjack value of the card
     */
    public int getValue() {
        return rank.getValue();
    }
    
    /**
     * String representation of the card.
     * @return String in format "SUIT-RANK" (e.g., "HEARTS-A", "CLUBS-10")
     */
    @Override
    public String toString() {
        return suit + "-" + rank;
    }
    
    /**
     * Check if this card equals another object.
     * Two cards are equal if they have the same suit and rank.
     * @param obj The object to compare with
     * @return true if the cards are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Card card = (Card) obj;
        return suit == card.suit && rank == card.rank;
    }
    
    /**
     * Generate hash code for this card.
     * Based on suit and rank to ensure uniqueness.
     * @return The hash code of the card
     */
    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }
}
