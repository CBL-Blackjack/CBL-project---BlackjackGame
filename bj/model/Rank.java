package bj.model;

/**
 * Represents the ranks of playing cards with their blackjack values.
 */
public enum Rank {
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(10),
    QUEEN(10),
    KING(10),
    ACE(11);
    
    private final int value;
    
    /**
     * Constructor for Rank enum.
     * @param value The numeric value of the rank
     */
    Rank(int value) {
        this.value = value;
    }
    
    /**
     * Get the numeric value of this rank.
     * @return The integer value of the rank
     */
    public int getValue() {
        return value;
    }
}
