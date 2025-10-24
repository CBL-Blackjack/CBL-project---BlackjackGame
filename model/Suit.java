package bj.model;

import java.awt.Color;

/**
 * Represents the four suits of a standard playing card deck.
 */
public enum Suit {
    SPADES("♠", Color.BLACK),
    HEARTS("♥", Color.RED),
    DIAMONDS("♦", Color.RED),
    CLUBS("♣", Color.BLACK);
    
    private final String symbol;
    private final Color color;
    
    /**
     * Constructor for Suit enum.
     * @param symbol The Unicode symbol for the suit
     * @param color The color for the suit (red or black)
     */
    Suit(String symbol, Color color) {
        this.symbol = symbol;
        this.color = color;
    }
    
    /**
     * Get the Unicode symbol for this suit.
     * @return The suit symbol (♠, ♥, ♦, ♣)
     */
    public String getSymbol() {
        return symbol;
    }
    
    /**
     * Get the color for this suit.
     * @return The color (red for hearts/diamonds, black for spades/clubs)
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Check if this suit is red.
     * @return true if the suit is red (hearts or diamonds), false otherwise
     */
    public boolean isRed() {
        return color == Color.RED;
    }
}
