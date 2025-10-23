package bj.model;

/**
 * Represents the ranks of playing cards with their blackjack values and display labels.
 */
public enum Rank {
    ACE(11, "A"),
    KING(10, "K"),
    QUEEN(10, "Q"),
    JACK(10, "J"),
    TEN(10, "10"),
    NINE(9, "9"),
    EIGHT(8, "8"),
    SEVEN(7, "7"),
    SIX(6, "6"),
    FIVE(5, "5"),
    FOUR(4, "4"),
    THREE(3, "3"),
    TWO(2, "2");
    
    private final int value;
    private final String shortLabel;
    
    /**
     * Constructor for Rank enum.
     * @param value The blackjack value of the rank
     * @param shortLabel The short label for display on cards
     */
    Rank(int value, String shortLabel) {
        this.value = value;
        this.shortLabel = shortLabel;
    }
    
    /**
     * Get the blackjack value of this rank.
     * @return The integer value of the rank
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Get the short label for display on cards.
     * @return The short label (e.g., "A", "K", "Q", "J", "10", "9", etc.)
     */
    public String getShortLabel() {
        return shortLabel;
    }
}
