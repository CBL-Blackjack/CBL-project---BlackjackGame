package bj.ui.bet;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Animated chip sprite that can move from rack to bet circle and back.
 * Handles its own rendering and animation logic.
 */
public class ChipSprite {
    
    private double x, y;
    private double targetX, targetY;
    private double vx, vy;
    private final int denomination;
    private final int radius;
    private final Color baseColor;
    private boolean arrived = false;
    private boolean animatingBack = false;
    
    // Animation constants
    private static final double EASING_FACTOR = 8.0;
    private static final double MAX_SPEED = 400.0;
    private static final double ARRIVAL_THRESHOLD = 8.0;
    
    public ChipSprite(int denomination, double startX, double startY) {
        this.denomination = denomination;
        this.radius = BetManager.getRadiusForDenomination(denomination);
        this.baseColor = BetManager.getColorForDenomination(denomination);
        this.x = startX;
        this.y = startY;
        this.targetX = startX;
        this.targetY = startY;
        this.vx = 0;
        this.vy = 0;
    }
    
    /**
     * Update chip position based on delta time
     */
    public void update(double dt) {
        if (arrived && !animatingBack) {
            return;
        }
        
        // Calculate distance to target
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Check if we've arrived
        if (distance < ARRIVAL_THRESHOLD) {
            x = targetX;
            y = targetY;
            vx = 0;
            vy = 0;
            arrived = true;
            return;
        }
        
        // Calculate velocity with easing
        double targetVx = dx * EASING_FACTOR;
        double targetVy = dy * EASING_FACTOR;
        
        // Limit maximum speed
        double speed = Math.sqrt(targetVx * targetVx + targetVy * targetVy);
        if (speed > MAX_SPEED) {
            targetVx = (targetVx / speed) * MAX_SPEED;
            targetVy = (targetVy / speed) * MAX_SPEED;
        }
        
        vx = targetVx;
        vy = targetVy;
        
        // Update position
        x += vx * dt;
        y += vy * dt;
    }
    
    /**
     * Set target position for animation
     */
    public void setTarget(double targetX, double targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.arrived = false;
    }
    
    /**
     * Set target position from Point
     */
    public void setTarget(java.awt.Point target) {
        setTarget(target.x, target.y);
    }
    
    /**
     * Check if chip has arrived at target
     */
    public boolean hasArrived() {
        return arrived;
    }
    
    /**
     * Start animating back to rack
     */
    public void animateBack() {
        animatingBack = true;
        arrived = false;
    }
    
    /**
     * Check if chip is animating back to rack
     */
    public boolean isAnimatingBack() {
        return animatingBack;
    }
    
    /**
     * Paint the chip
     */
    public void paint(Graphics2D g2) {
        Graphics2D g2d = (Graphics2D) g2.create();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = (int) Math.round(x);
        int centerY = (int) Math.round(y);
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 80));
        Ellipse2D shadow = new Ellipse2D.Float(centerX - radius + 2, centerY - radius + 3, 
                                             radius * 2, radius * 2);
        g2d.fill(shadow);
        
        // Draw main chip body
        g2d.setColor(baseColor);
        Ellipse2D chip = new Ellipse2D.Float(centerX - radius, centerY - radius, 
                                           radius * 2, radius * 2);
        g2d.fill(chip);
        
        // Draw white ring
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(chip);
        
        // Draw inner ring
        int innerRadius = radius - 4;
        Ellipse2D innerRing = new Ellipse2D.Float(centerX - innerRadius, centerY - innerRadius, 
                                                innerRadius * 2, innerRadius * 2);
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(innerRing);
        
        // Draw edge ticks (small marks around the edge)
        drawEdgeTicks(g2d, centerX, centerY, radius);
        
        // Draw denomination text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(10, radius / 2)));
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(denomination);
        int textX = centerX - fm.stringWidth(text) / 2;
        int textY = centerY + fm.getAscent() / 2 - 2;
        g2d.drawString(text, textX, textY);
        
        g2d.dispose();
    }
    
    /**
     * Draw small edge ticks around the chip
     */
    private void drawEdgeTicks(Graphics2D g2d, int centerX, int centerY, int radius) {
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(1.0f));
        
        int tickCount = 12;
        int tickRadius = radius - 2;
        
        for (int i = 0; i < tickCount; i++) {
            double angle = (2.0 * Math.PI * i) / tickCount;
            int x1 = centerX + (int) (Math.cos(angle) * tickRadius);
            int y1 = centerY + (int) (Math.sin(angle) * tickRadius);
            int x2 = centerX + (int) (Math.cos(angle) * (tickRadius - 3));
            int y2 = centerY + (int) (Math.sin(angle) * (tickRadius - 3));
            
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
    
    // Getters
    public int getDenomination() {
        return denomination;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public Color getBaseColor() {
        return baseColor;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public java.awt.Point getPosition() {
        return new java.awt.Point((int) Math.round(x), (int) Math.round(y));
    }
}
