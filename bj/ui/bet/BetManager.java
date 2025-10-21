package bj.ui.bet;

import bj.logic.GameState;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages betting logic and chip tracking for the blackjack game.
 * Pure logic - no UI rendering.
 */
public class BetManager {
    
    private int currentBet = 0;
    private int maxBet = 0; // Optional limit
    private final List<ChipSprite> placedChips = new ArrayList<>();
    
    /**
     * Check if a chip of the given denomination can be placed
     */
    public boolean canPlace(int denom, int bankroll, GameState state) {
        // Only allow betting in ROUND_START state
        if (state != GameState.ROUND_START) {
            return false;
        }
        
        // Check if denomination is valid
        if (!isValidDenomination(denom)) {
            return false;
        }
        
        // Check if adding this chip would exceed bankroll
        if (currentBet + denom > bankroll) {
            return false;
        }
        
        // Check optional max bet limit
        if (maxBet > 0 && currentBet + denom > maxBet) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Add a chip to the current bet
     */
    public void addChip(int denom) {
        if (isValidDenomination(denom)) {
            currentBet += denom;
            // Note: ChipSprite will be added externally via addChipSprite()
        }
    }
    
    /**
     * Add a chip sprite to the placed chips list
     */
    public void addChipSprite(ChipSprite chip) {
        placedChips.add(chip);
    }
    
    /**
     * Remove the last chip from the bet
     */
    public void removeLastChip() {
        if (!placedChips.isEmpty()) {
            ChipSprite lastChip = placedChips.remove(placedChips.size() - 1);
            currentBet -= lastChip.getDenomination();
        }
    }
    
    /**
     * Clear all chips and reset bet to zero
     */
    public void clearAll() {
        currentBet = 0;
        placedChips.clear();
    }
    
    /**
     * Get the current bet amount
     */
    public int getCurrentBet() {
        return currentBet;
    }
    
    /**
     * Get all placed chip sprites
     */
    public List<ChipSprite> getChips() {
        return new ArrayList<>(placedChips);
    }
    
    /**
     * Get the number of chips placed
     */
    public int getChipCount() {
        return placedChips.size();
    }
    
    /**
     * Check if a denomination is valid
     */
    private boolean isValidDenomination(int denom) {
        return denom == 5 || denom == 10 || denom == 25 || denom == 100;
    }
    
    /**
     * Get the color for a given denomination
     */
    public static java.awt.Color getColorForDenomination(int denom) {
        return switch (denom) {
            case 5 -> new java.awt.Color(200, 50, 50);   // RED
            case 10 -> new java.awt.Color(50, 50, 200);  // BLUE
            case 25 -> new java.awt.Color(50, 150, 50);  // GREEN
            case 100 -> new java.awt.Color(50, 50, 50);  // BLACK
            default -> new java.awt.Color(128, 128, 128); // GRAY
        };
    }
    
    /**
     * Get the radius for a given denomination
     */
    public static int getRadiusForDenomination(int denom) {
        return switch (denom) {
            case 5 -> 18;
            case 10 -> 20;
            case 25 -> 22;
            case 100 -> 24;
            default -> 18;
        };
    }
    
    /**
     * Set optional maximum bet limit
     */
    public void setMaxBet(int maxBet) {
        this.maxBet = maxBet;
    }
    
    /**
     * Get the maximum bet limit
     */
    public int getMaxBet() {
        return maxBet;
    }
}
