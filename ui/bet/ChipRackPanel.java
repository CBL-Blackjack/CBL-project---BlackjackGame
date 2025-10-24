package bj.ui.bet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import bj.ui.TablePanel;

/**
 * Chip rack panel that displays chips in a horizontal arc in front of the player.
 * Positioned as a transparent overlay on the table, bottom center.
 */
public class ChipRackPanel extends JPanel implements MouseListener, MouseMotionListener {
    
    // Chip denominations and colors
    private static final int[] DENOMINATIONS = {5, 10, 25, 100};
    private static final Color[] CHIP_COLORS = {
        new Color(190, 40, 40),    // bl5 = RED
        new Color(40, 80, 200),    // 10 = BLUE
        new Color(50, 160, 80),    // 25 = GREEN
        new Color(40, 40, 40)      // 100 = BLACK
    };
    
    private static final int CHIP_RADIUS = 32;
    private static final int CHIP_GAP = 14;
    
    private final TablePanel tablePanel;
    private java.util.function.Consumer<Integer> onChipPlaceCallback;
    private boolean enabled = true;
    
    // Chip areas for click detection
    private List<ChipArea> chipAreas = new ArrayList<>();
    private int hoveredChipIndex = -1;
    private Timer hoverTimer;
    private Timer clickFeedbackTimer;
    private int clickedChipIndex = -1;
    private double hoverScale = 1.0;
    
    // Tray properties
    private java.awt.geom.RoundRectangle2D.Double trayShape;
    private Rectangle trayRect;
    private int trayX, trayY, trayW, trayH;
    
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
    
    public ChipRackPanel(TablePanel tablePanel) {
        this.tablePanel = tablePanel;
        
        setupPanel();
        setupHoverAnimation();
        
        // Disable tooltips globally
        setToolTipText(null);
    }
    
    private void setupPanel() {
        setOpaque(false);
        setFocusable(false); // Don't steal focus from HUD buttons
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        // Add mouse listeners for chip interaction
        addMouseListener(this);
        addMouseMotionListener(this);
        
        // Initialize chip centers
        calculateChipPositions();
    }
    
    private void setupHoverAnimation() {
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
     * Calculate chip positions inside a tray on the left side
     */
    private void calculateChipPositions() {
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) {
            return; // Wait for proper sizing
        }
        
        // Calculate default tray dimensions
        int trayW = Math.max(340, Math.min(width * 3 / 10, 420));
        int trayH = 120;
        int trayX = 28;
        int trayY = height - trayH - 32;
        
        calculateChipPositions(trayX, trayY, trayW, trayH);
    }
    
    /**
     * Calculate chip positions with specified zone parameters
     */
    private void calculateChipPositions(int leftZoneX, int leftZoneY, int leftZoneW, int leftZoneH) {
        chipAreas.clear();
        
        // Use provided zone dimensions
        trayX = leftZoneX;
        trayY = leftZoneY;
        trayW = leftZoneW;
        trayH = leftZoneH;
        
        // Create tray shape
        trayShape = new java.awt.geom.RoundRectangle2D.Double(
            trayX, trayY, trayW, trayH, 24, 24);
        trayRect = new Rectangle(trayX, trayY, trayW, trayH);
        
        // Position chips inside the tray
        int r = CHIP_RADIUS; // chip radius
        int d = 2 * r; // chip diameter
        int gap = CHIP_GAP;
        int cx0 = trayX + (trayW - (4 * d + 3 * gap)) / 2 + r; // first center X
        int cy = trayY + trayH / 2 + 6; // slightly below center
        
        for (int i = 0; i < DENOMINATIONS.length; i++) {
            int cx = cx0 + i * (d + gap);
            
            Ellipse2D.Double chipShape = new Ellipse2D.Double(
                cx - r, cy - r, 2 * r, 2 * r);
            
            chipAreas.add(new ChipArea(chipShape, DENOMINATIONS[i], CHIP_COLORS[i]));
        }
    }
    
    /**
     * Recalculate chip positions when panel is resized
     */
    public void recalculatePositions() {
        calculateChipPositions();
        repaint();
    }
    
    /**
     * Recalculate chip positions with zone parameters
     */
    public void recalculatePositions(int leftZoneX, int leftZoneY, int leftZoneW, int leftZoneH) {
        calculateChipPositions(leftZoneX, leftZoneY, leftZoneW, leftZoneH);
        repaint();
    }
    
    /**
     * Set the tray rectangle bounds
     */
    public void setTrayRect(Rectangle rect) {
        this.trayX = rect.x;
        this.trayY = rect.y;
        this.trayW = rect.width;
        this.trayH = rect.height;
        this.trayShape = new java.awt.geom.RoundRectangle2D.Double(
            trayX, trayY, trayW, trayH, 24, 24);
        this.trayRect = new Rectangle(rect);
        calculateChipPositions(trayX, trayY, trayW, trayH);
        repaint();
    }
    
    /**
     * Override tooltip to always return null (disable tooltips)
     */
    @Override
    public String getToolTipText(java.awt.event.MouseEvent event) {
        return null;
    }
    
    /**
     * Place a chip of the given denomination
     */
    private void placeChip(int denomination) {
        if (onChipPlaceCallback != null && enabled) {
            onChipPlaceCallback.accept(denomination);
        }
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
    
    /**
     * Set callback for chip placement
     */
    public void setOnChipPlaceCallback(java.util.function.Consumer<Integer> callback) {
        this.onChipPlaceCallback = callback;
    }
    
    /**
     * Enable or disable chip rack interaction
     */
    public void setEnabledRack(boolean enabled) {
        this.enabled = enabled;
        setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) : 
                           Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    // MouseListener implementation
    @Override
    public void mouseClicked(MouseEvent e) {
        if (!enabled) return;
        
        // Check if click is inside the tray
        if (trayRect != null && !trayRect.contains(e.getPoint())) {
            return; // Ignore clicks outside the tray
        }
        
        int chipIndex = getChipAtPoint(e.getPoint());
        if (chipIndex >= 0 && chipIndex < chipAreas.size()) {
            // Show click feedback
            clickedChipIndex = chipIndex;
            clickFeedbackTimer.stop();
            clickFeedbackTimer.start();
            repaint();
            
            // Place the chip
            ChipArea chipArea = chipAreas.get(chipIndex);
            placeChip(chipArea.denomination);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {}
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {
        hoveredChipIndex = -1;
        repaint();
    }
    
    // MouseMotionListener implementation
    @Override
    public void mouseDragged(MouseEvent e) {}
    
    @Override
    public void mouseMoved(MouseEvent e) {
        if (!enabled) return;
        
        int chipIndex = -1;
        
        // Only check for hover if pointer is inside the tray
        if (trayRect != null && trayRect.contains(e.getPoint())) {
            chipIndex = getChipAtPoint(e.getPoint());
        }
        
        if (chipIndex != hoveredChipIndex) {
            hoveredChipIndex = chipIndex;
            setCursor(chipIndex >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : 
                                 Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            repaint();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Recalculate chip positions if needed
        if (chipAreas.isEmpty()) {
            calculateChipPositions();
        }
        
        // Draw the chip tray
        drawTray(g2d);
        
        // Draw chips clipped to tray
        if (trayShape != null) {
            // Save current clip
            Shape originalClip = g2d.getClip();
            
            // Set clip to tray shape
            g2d.setClip(trayShape);
            
            // Draw each chip
            for (int i = 0; i < chipAreas.size(); i++) {
                ChipArea chipArea = chipAreas.get(i);
                boolean isHovered = (i == hoveredChipIndex);
                boolean isClicked = (i == clickedChipIndex);
                double scale = isHovered ? hoverScale : (isClicked ? 1.1 : 1.0);
                
                drawChipFromArea(g2d, chipArea, scale, isHovered);
            }
            
            // Restore original clip
            g2d.setClip(originalClip);
        }
        
        g2d.dispose();
    }
    
    /**
     * Draw the chip tray
     */
    private void drawTray(Graphics2D g2d) {
        if (trayShape == null) return;
        
        // Draw outer shadow/halo
        g2d.setColor(new Color(0, 0, 0, 60));
        java.awt.geom.RoundRectangle2D.Double shadowShape = new java.awt.geom.RoundRectangle2D.Double(
            trayX + 3, trayY + 4, trayW, trayH, 24, 24);
        g2d.fill(shadowShape);
        
        // Draw main tray background
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fill(trayShape);
        
        // Draw inner stroke
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(trayShape);
        
        // Draw "CHIPS" title
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        String title = "CHIPS";
        int titleX = trayX + 20;
        int titleY = trayY + 18;
        g2d.drawString(title, titleX, titleY);
    }
    
    /**
     * Draw a chip from its ChipArea
     */
    private void drawChipFromArea(Graphics2D g2d, ChipArea chipArea, double scale, boolean hovered) {
        Ellipse2D.Double shape = chipArea.shape;
        int denomination = chipArea.denomination;
        Color baseColor = chipArea.color;
        
        // Calculate scaled dimensions
        double centerX = shape.getCenterX();
        double centerY = shape.getCenterY();
        int radius = (int) (CHIP_RADIUS * scale);
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 80));
        Ellipse2D shadow = new Ellipse2D.Double(
            centerX - radius + 3, centerY - radius + 4, 
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
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.draw(chip);
        
        // Draw inner ring
        int innerRadius = radius - 6;
        Ellipse2D innerRing = new Ellipse2D.Double(
            centerX - innerRadius, centerY - innerRadius, 
            innerRadius * 2, innerRadius * 2);
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(innerRing);
        
        // Draw glossy highlight
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.setStroke(new BasicStroke(1.5f));
        Ellipse2D highlight = new Ellipse2D.Double(
            centerX - innerRadius + 5, centerY - innerRadius + 5, 
            innerRadius - 10, innerRadius - 10);
        g2d.draw(highlight);
        
        // Draw denomination text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(denomination);
        int textX = (int) (centerX - fm.stringWidth(text) / 2);
        int textY = (int) (centerY + fm.getAscent() / 2 - 2);
        
        // Draw text shadow
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(text, textX + 1, textY + 1);
        
        // Draw main text
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, textX, textY);
        
        // Draw hover glow
        if (hovered) {
            g2d.setColor(new Color(255, 255, 255, 60));
            g2d.setStroke(new BasicStroke(4.0f));
            Ellipse2D glow = new Ellipse2D.Double(
                centerX - radius - 5, centerY - radius - 5, 
                radius * 2 + 10, radius * 2 + 10);
            g2d.draw(glow);
        }
    }
    
    
}
