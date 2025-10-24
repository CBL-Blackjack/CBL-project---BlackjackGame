package bj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A standalone chip tray widget that manages its own size and drawing.
 * Draws four chip denominations (5/10/25/100) and handles mouse interactions.
 */
public class ChipTrayWidget extends JPanel {
    
    // Chip denominations and colors
    private static final int[] DENOMINATIONS = {5, 10, 25, 100};
    private static final Color[] CHIP_COLORS = {
        new Color(190, 40, 40),    // 5 = RED
        new Color(40, 80, 200),    // 10 = BLUE
        new Color(50, 160, 80),    // 25 = GREEN
        new Color(40, 40, 40)      // 100 = BLACK
    };
    
    private static final int CHIP_RADIUS = 28;
    private static final int CHIP_GAP = 12;
    
    private Consumer<Integer> onChipClick;
    private boolean enabled = true;
    
    // Chip areas for click detection
    private List<ChipArea> chipAreas = new ArrayList<>();
    private int hoveredChipIndex = -1;
    private Timer hoverTimer;
    private int clickedChipIndex = -1;
    private Timer clickFeedbackTimer;
    private double hoverScale = 1.0;
    
    /**
     * Inner class to represent a clickable chip area
     */
    private static class ChipArea {
        final Ellipse2D.Double shape;
        final int denomination;
        final Color color;
        
        ChipArea(Ellipse2D.Double shape, int denomination, Color color) {
            this.shape = shape;
            this.denomination = denomination;
            this.color = color;
        }
    }
    
    public ChipTrayWidget() {
        setOpaque(false);
        setPreferredSize(new Dimension(420, 120));
        setMinimumSize(new Dimension(320, 110));
        setMaximumSize(new Dimension(480, 140));
        setFocusable(false); // Prevent stealing focus from buttons
        
        // Add mouse listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!enabled) return;
                
                // Only handle clicks on actual chips, don't consume other events
                int chipIndex = getChipAtPoint(e.getPoint());
                if (chipIndex >= 0 && chipIndex < chipAreas.size()) {
                    // Show click feedback
                    clickedChipIndex = chipIndex;
                    clickFeedbackTimer.stop();
                    clickFeedbackTimer.start();
                    repaint();
                    
                    // Trigger chip click
                    if (onChipClick != null) {
                        onChipClick.accept(chipAreas.get(chipIndex).denomination);
                    }
                }
                // Do not consume events outside chip areas - let them pass through
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!enabled) return;
                
                int chipIndex = getChipAtPoint(e.getPoint());
                if (chipIndex != hoveredChipIndex) {
                    hoveredChipIndex = chipIndex;
                    setCursor(chipIndex >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : 
                                     Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
        });
        
        // Setup hover animation
        hoverTimer = new Timer(16, e -> {
            if (hoveredChipIndex >= 0) {
                hoverScale = Math.min(1.05, hoverScale + 0.05);
            } else {
                hoverScale = Math.max(1.0, hoverScale - 0.05);
            }
            repaint();
        });
        hoverTimer.start();
        
        // Click feedback timer
        clickFeedbackTimer = new Timer(150, e -> {
            clickedChipIndex = -1;
            repaint();
        });
    }
    
    /**
     * Set the callback for chip clicks
     */
    public void setOnChipClick(Consumer<Integer> callback) {
        this.onChipClick = callback;
    }
    
    /**
     * Enable or disable chip interactions
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) : 
                           Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    /**
     * Check if a point is inside a chip area
     */
    private int getChipAtPoint(Point point) {
        for (int i = 0; i < chipAreas.size(); i++) {
            ChipArea chipArea = chipAreas.get(i);
            if (chipArea.shape.contains(point)) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Calculate chip positions
        calculateChipPositions();
        
        // Draw each chip
        for (int i = 0; i < chipAreas.size(); i++) {
            ChipArea chipArea = chipAreas.get(i);
            boolean isHovered = (i == hoveredChipIndex);
            boolean isClicked = (i == clickedChipIndex);
            double scale = isHovered ? hoverScale : (isClicked ? 1.1 : 1.0);
            
            drawChip(g2d, chipArea, scale, isHovered);
        }
        
        g2d.dispose();
    }
    
    /**
     * Calculate chip positions based on current widget size
     */
    private void calculateChipPositions() {
        chipAreas.clear();
        
        int width = getWidth();
        int height = getHeight();
        
        if (width <= 0 || height <= 0) {
            return;
        }
        
        // Position chips horizontally centered
        int r = CHIP_RADIUS;
        int d = 2 * r;
        int gap = CHIP_GAP;
        int totalWidth = DENOMINATIONS.length * d + (DENOMINATIONS.length - 1) * gap;
        int startX = (width - totalWidth) / 2 + r;
        int centerY = height / 2;
        
        for (int i = 0; i < DENOMINATIONS.length; i++) {
            int x = startX + i * (d + gap);
            
            Ellipse2D.Double chipShape = new Ellipse2D.Double(
                x - r, centerY - r, 2 * r, 2 * r);
            
            chipAreas.add(new ChipArea(chipShape, DENOMINATIONS[i], CHIP_COLORS[i]));
        }
    }
    
    /**
     * Draw a chip with proper styling
     */
    private void drawChip(Graphics2D g2d, ChipArea chipArea, double scale, boolean hovered) {
        Ellipse2D.Double shape = chipArea.shape;
        int denomination = chipArea.denomination;
        Color baseColor = chipArea.color;
        
        // Calculate scaled dimensions
        double centerX = shape.getCenterX();
        double centerY = shape.getCenterY();
        int radius = (int) (CHIP_RADIUS * scale);
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 60));
        Ellipse2D shadow = new Ellipse2D.Double(
            centerX - radius + 2, centerY - radius + 3, 
            radius * 2, radius * 2);
        g2d.fill(shadow);
        
        // Draw main chip body
        g2d.setColor(baseColor);
        Ellipse2D chip = new Ellipse2D.Double(
            centerX - radius, centerY - radius, 
            radius * 2, radius * 2);
        g2d.fill(chip);
        
        // Draw white ring
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.draw(chip);
        
        // Draw inner ring
        int innerRadius = radius - 5;
        Ellipse2D innerRing = new Ellipse2D.Double(
            centerX - innerRadius, centerY - innerRadius, 
            innerRadius * 2, innerRadius * 2);
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(innerRing);
        
        // Draw denomination text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(denomination);
        int textX = (int) (centerX - fm.stringWidth(text) / 2);
        int textY = (int) (centerY + fm.getAscent() / 2 - 1);
        
        // Draw text shadow
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.drawString(text, textX + 1, textY + 1);
        
        // Draw main text
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, textX, textY);
        
        // Draw hover glow
        if (hovered) {
            g2d.setColor(new Color(255, 255, 255, 40));
            g2d.setStroke(new BasicStroke(3.0f));
            Ellipse2D glow = new Ellipse2D.Double(
                centerX - radius - 3, centerY - radius - 3, 
                radius * 2 + 6, radius * 2 + 6);
            g2d.draw(glow);
        }
    }
}
