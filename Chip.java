package bj.ui;

import java.awt.*;

/**
 * A simple model object for an animated betting chip.
 * Handles movement animation from spawn position to target position with easing.
 * 
 * <p>This class provides a pure data model with draw method for rendering.
 * Animation is controlled externally via the {@code update(double dt)} method
 * which applies easing physics to create smooth movement toward the target.</p>
 * 
 * <p><b>Swing-only constraint:</b uses only java.awt.Graphics2D for rendering,
 * no external graphics libraries.</p>
 * 
 * <p>Key features:
 * <ul>
 * <li>Easing animation with configurable speed limits</li>
 * <li>Automatic arrival detection and position snapping</li>
 * <li>Self-contained rendering with Graphics2D</li>
 * <li>Pure data model - no UI dependencies</li>
 * </ul></p>
 */
public class Chip {
    
    // Position and movement fields
    public double x, y;              // Current position
    public double targetX, targetY;  // Target position to move toward
    public double vx, vy;            // Current velocity
    public int radius;               // Chip radius
    public Color color;              // Chip color
    public boolean arrived;          // Whether chip has reached target
    
    // Animation constants
    private static final double EASING_FACTOR = 6.0;
    private static final double MAX_SPEED = 200.0;
    
    /**
     * Constructor for creating a betting chip
     * @param spawnX Initial X position (spawn location)
     * @param spawnY Initial Y position (spawn location)
     * @param targetX Target X position (bet center)
     * @param targetY Target Y position (bet center)
     * @param radius Chip radius (e.g., 14)
     * @param color Chip color (e.g., Color.RED or Color.BLUE)
     */
    public Chip(double spawnX, double spawnY, double targetX, double targetY, int radius, Color color) {
        this.x = spawnX;
        this.y = spawnY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.radius = radius;
        this.color = color;
        this.vx = 0;
        this.vy = 0;
        this.arrived = false;
    }
    
    /**
     * Update chip position based on elapsed time.
     * Moves toward target position with easing animation.
     * 
     * <p>Applies easing physics to create smooth movement. When the chip
     * arrives at the target (within radius/2), it snaps to the exact target
     * position and stops moving.</p>
     * 
     * @param dt Delta time in seconds since last update
     */
    public void update(double dt) {
        if (arrived) {
            return;
        }
        
        // Calculate distance to target
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Check if we've arrived at target
        if (distance < radius / 2.0) {
            // Snap to target and stop moving
            x = targetX;
            y = targetY;
            vx = 0;
            vy = 0;
            arrived = true;
            return;
        }
        
        // Normalize direction vector
        if (distance > 0) {
            dx /= distance;
            dy /= distance;
        }
        
        // Calculate velocity with easing
        double targetVx = EASING_FACTOR * dx * distance;
        double targetVy = EASING_FACTOR * dy * distance;
        
        // Clamp velocity to max speed
        double speed = Math.sqrt(targetVx * targetVx + targetVy * targetVy);
        if (speed > MAX_SPEED) {
            targetVx = (targetVx / speed) * MAX_SPEED;
            targetVy = (targetVy / speed) * MAX_SPEED;
        }
        
        // Update velocity
        vx = targetVx;
        vy = targetVy;
        
        // Update position
        x += vx * dt;
        y += vy * dt;
    }
    
    /**
     * Draw the chip on the graphics context.
     * Renders a filled circle with white ring and center mark.
     * 
     * <p>Uses only standard Graphics2D operations for rendering,
     * ensuring compatibility with the Swing-only constraint.</p>
     * 
     * @param g2 Graphics2D context for drawing
     */
    public void draw(Graphics2D g2) {
        // Enable antialiasing for smooth circles
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = (int) Math.round(x);
        int centerY = (int) Math.round(y);
        
        // Draw outer white ring
        g2.setColor(Color.WHITE);
        g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Draw main chip body with specified color
        g2.setColor(color);
        g2.fillOval(centerX - radius + 2, centerY - radius + 2, (radius - 2) * 2, (radius - 2) * 2);
        
        // Draw center mark
        g2.setColor(Color.WHITE);
        int centerMarkRadius = radius / 3;
        g2.fillOval(centerX - centerMarkRadius, centerY - centerMarkRadius, 
                   centerMarkRadius * 2, centerMarkRadius * 2);
        
        // Draw center dot
        g2.setColor(Color.BLACK);
        int dotRadius = radius / 6;
        g2.fillOval(centerX - dotRadius, centerY - dotRadius, 
                   dotRadius * 2, dotRadius * 2);
    }
    
    /**
     * Check if the chip has arrived at its target position
     * @return true if chip has reached target, false otherwise
     */
    public boolean hasArrived() {
        return arrived;
    }
    
    /**
     * Get the current distance to target
     * @return Distance to target position
     */
    public double getDistanceToTarget() {
        double dx = targetX - x;
        double dy = targetY - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Reset chip to a new target position
     * @param newTargetX New target X position
     * @param newTargetY New target Y position
     */
    public void setTarget(double newTargetX, double newTargetY) {
        this.targetX = newTargetX;
        this.targetY = newTargetY;
        this.arrived = false;
    }
}
