package bj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import bj.ui.bet.ChipSprite;

/**
 * Manages animation of betting chips on a blackjack table.
 * Handles chip spawning, movement, and rendering using EDT-friendly animation.
 * 
 * <p>Uses {@code javax.swing.Timer} for animation to ensure all updates occur
 * on the Event Dispatch Thread (EDT), preventing thread safety issues and
 * maintaining smooth UI responsiveness. The timer runs at ~60 FPS for fluid
 * chip movement with easing animation.</p>
 * 
 * <p><b>Swing-only constraint:</b uses only javax.swing.Timer, no external animation libraries.</p>
 * 
 * <p>Key features:
 * <ul>
 * <li>Automatic chip spawning and movement animation</li>
 * <li>EDT-safe updates via Swing Timer</li>
 * <li>Automatic cleanup of arrived chips</li>
 * <li>Integration with TablePanel for rendering</li>
 * </ul></p>
 */
public class TableAnimator implements ActionListener {
    
    private TablePanel table;
    private List<Chip> chips;
    private List<ChipSprite> chipSprites;
    private Timer timer;
    private long lastTickTime;
    
    // Timer configuration
    private static final int TIMER_DELAY = 16; // ~60 FPS
    
    public TableAnimator(TablePanel table) {
        this.table = table;
        this.chips = new ArrayList<>();
        this.chipSprites = new ArrayList<>();
        this.timer = null;
        this.lastTickTime = System.nanoTime();
    }
    
    /**
     * Start the animation timer.
     * 
     * <p>Creates and starts a {@code javax.swing.Timer} that runs on the EDT,
     * ensuring all animation updates are thread-safe and don't block the UI.</p>
     */
    public void start() {
        if (timer == null) {
            timer = new Timer(TIMER_DELAY, this);
            lastTickTime = System.nanoTime();
        }
        if (!timer.isRunning()) {
            timer.start();
        }
    }
    
    /**
     * Stop the animation timer
     */
    public void stop() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }
    
    /**
     * Check if the animator is currently running
     * @return true if timer is running, false otherwise
     */
    public boolean isRunning() {
        return timer != null && timer.isRunning();
    }
    
    /**
     * Timer callback that runs on the EDT.
     * 
     * <p>This method is called by the Swing Timer at ~60 FPS intervals.
     * It updates all chip positions and triggers a repaint, ensuring
     * smooth animation without blocking the UI thread.</p>
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Calculate delta time in seconds
        long currentTime = System.nanoTime();
        double dt = (currentTime - lastTickTime) / 1_000_000_000.0;
        lastTickTime = currentTime;
        
        // Update all chips
        updateChips(dt);
        
        // Update all chip sprites
        updateChipSprites(dt);
        
        // Remove arrived chips (optional - you can comment this out to keep them stacked)
        removeArrivedChips();
        
        // Remove arrived chip sprites
        removeArrivedChipSprites();
        
        // Repaint the table
        if (table != null) {
            table.repaint();
        }
    }
    
    /**
     * Update all chips with the given delta time
     * @param dt Delta time in seconds
     */
    private void updateChips(double dt) {
        for (Chip chip : chips) {
            chip.update(dt);
        }
    }
    
    /**
     * Update all chip sprites with the given delta time
     * @param dt Delta time in seconds
     */
    private void updateChipSprites(double dt) {
        for (ChipSprite chipSprite : chipSprites) {
            chipSprite.update(dt);
        }
    }
    
    /**
     * Remove chips that have arrived at their target
     * Comment out this method call in actionPerformed() if you want to keep arrived chips
     */
    private void removeArrivedChips() {
        chips.removeIf(Chip::hasArrived);
    }
    
    /**
     * Remove chip sprites that have arrived at their target
     */
    private void removeArrivedChipSprites() {
        chipSprites.removeIf(ChipSprite::hasArrived);
    }
    
    /**
     * Spawn a new chip at the given start position moving toward the target position
     * @param start Starting position of the chip
     * @param target Target position for the chip to move to
     * @param color Color of the chip
     */
    public void spawnChip(Point start, Point target, Color color) {
        Chip chip = new Chip(start.x, start.y, target.x, target.y, 14, color);
        chips.add(chip);
    }
    
    /**
     * Spawn a new chip with custom radius
     * @param start Starting position of the chip
     * @param target Target position for the chip to move to
     * @param color Color of the chip
     * @param radius Radius of the chip
     */
    public void spawnChip(Point start, Point target, Color color, int radius) {
        Chip chip = new Chip(start.x, start.y, target.x, target.y, radius, color);
        chips.add(chip);
    }
    
    /**
     * Draw all chips on the graphics context
     * @param g2 Graphics2D context for drawing
     */
    public void drawAll(Graphics2D g2) {
        for (Chip chip : chips) {
            chip.draw(g2);
        }
        for (ChipSprite chipSprite : chipSprites) {
            chipSprite.paint(g2);
        }
    }
    
    /**
     * Get the list of chips (for external access)
     * @return List of all chips
     */
    public List<Chip> getChips() {
        return chips;
    }
    
    /**
     * Get the number of active chips
     * @return Number of chips currently being animated
     */
    public int getChipCount() {
        return chips.size();
    }
    
    /**
     * Clear all chips from the animator
     */
    public void clearChips() {
        chips.clear();
    }
    
    /**
     * Get the table panel reference
     * @return The associated TablePanel
     */
    public TablePanel getTable() {
        return table;
    }
    
    /**
     * Set a new table panel reference
     * @param table The new TablePanel to associate with this animator
     */
    public void setTable(TablePanel table) {
        this.table = table;
    }
    
    /**
     * Spawn a new chip sprite
     * @param start Starting position
     * @param denomination Chip denomination
     * @param target Target position
     * @param color Chip color
     * @return The created ChipSprite
     */
    public ChipSprite spawnChipSprite(Point start, int denomination, Point target, Color color) {
        ChipSprite chipSprite = new ChipSprite(denomination, start.x, start.y);
        chipSprite.setTarget(target);
        chipSprites.add(chipSprite);
        return chipSprite;
    }
    
    /**
     * Remove a chip sprite
     * @param chipSprite The chip sprite to remove
     */
    public void removeChipSprite(ChipSprite chipSprite) {
        chipSprites.remove(chipSprite);
    }
    
    /**
     * Get all chip sprites
     * @return List of all chip sprites
     */
    public List<ChipSprite> getChipSprites() {
        return new ArrayList<>(chipSprites);
    }
    
    /**
     * Clear all chip sprites
     */
    public void clearChipSprites() {
        chipSprites.clear();
    }
}
