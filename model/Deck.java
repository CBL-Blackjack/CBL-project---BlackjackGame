package bj.model;

import java.util.*;

/**
 * Represents a deck of playing cards with standard 52-card composition.
 * Provides methods for drawing cards and managing deck state.
 */
public class Deck {
    
    private final Deque<Card> stack;
    
    /**
     * Constructor that builds a standard 52-card deck, shuffles it, and loads it into the stack.
     */
    public Deck() {
        List<Card> cards = buildStandard52();
        Collections.shuffle(cards);
        this.stack = new ArrayDeque<>(cards);
    }
    
    /**
     * Build a standard 52-card deck with all suits and ranks.
     * @return A list containing all 52 cards
     */
    private List<Card> buildStandard52() {
        List<Card> cards = new ArrayList<>();
        
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        
        return cards;
    }
    
    /**
     * Get the current size of the deck.
     * @return The number of cards remaining in the deck
     */
    public int size() {
        return stack.size();
    }
    
    /**
     * Check if the deck is empty.
     * @return true if the deck has no cards, false otherwise
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }
    
    /**
     * Draw a card from the top of the deck.
     * @return The card drawn from the top of the deck
     * @throws NoSuchElementException if the deck is empty
     */
    public Card draw() {
        if (stack.isEmpty()) {
            throw new NoSuchElementException("Cannot draw from empty deck");
        }
        return stack.pop();
    }
    
    /**
     * Reset the deck to a fresh standard 52-card deck and shuffle it.
     * Replaces all cards in the deck with a new shuffled set.
     */
    public void resetAndShuffle() {
        stack.clear();
        List<Card> cards = buildStandard52();
        Collections.shuffle(cards);
        stack.addAll(cards);
    }
}
