package bj.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A translucent bottom bar panel that provides a visual separation between
 * the game table and the control elements (chip tray and HUD buttons).
 * 
 * <p>This panel renders a dark translucent "table belt" across the bottom
 * of the screen with a subtle gradient and shadow effects. It defines
 * left and right zones for chip tray and HUD buttons respectively.</p>
 */
public class BottomBarPanel extends JPanel {
    
    // Band geometry
    private int bandY = 0;
    private int bandH = 0;
    
    // Zone rectangles for left (chips) and right (buttons) areas
    private Rectangle leftZone = new Rectangle();
    private Rectangle rightZone = new Rectangle();
    
    // Colors for the translucent band
    private static final Color BAND_TOP = new Color(0, 0, 0, 110);
    private static final Color BAND_BOTTOM = new Color(0, 0, 0, 150);
    private static final Color HIGHLIGHT_LINE = new Color(255, 255, 255, 40);
    
    public BottomBarPanel() {
        setOpaque(false);
        setLayout(null);
    }
    
    /**
     * Set the band geometry (Y position and height)
     * @param y The Y position of the band
     * @param h The height of the band
     */
    public void setBand(int y, int h) {
        this.bandY = y;
        this.bandH = h;
        repaint();
    }
    
    /**
     * Provide the left and right zones for chip tray and HUD buttons
     * @param leftZone The rectangle for the chip tray area
     * @param rightZone The rectangle for the HUD buttons area
     */
    public void provideZones(Rectangle leftZone, Rectangle rightZone) {
        this.leftZone = new Rectangle(leftZone);
        this.rightZone = new Rectangle(rightZone);
        repaint();
    }
    
    /**
     * Get the left zone rectangle (for chip tray)
     * @return The left zone rectangle
     */
    public Rectangle getLeftZone() {
        return new Rectangle(leftZone);
    }
    
    /**
     * Get the right zone rectangle (for HUD buttons)
     * @return The right zone rectangle
     */
    public Rectangle getRightZone() {
        return new Rectangle(rightZone);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (bandH <= 0) {
            return; // No band to draw
        }
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        
        // Save the original clip
        Shape originalClip = g2d.getClip();
        
        // Clip to band rectangle
        g2d.setClip(0, bandY, w, bandH);
        
        // Draw vertical gradient from top to bottom
        GradientPaint gradient = new GradientPaint(
            0, bandY, BAND_TOP,
            0, bandY + bandH, BAND_BOTTOM
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, bandY, w, bandH);
        
        // Draw soft top shadow (vignette effect)
        for (int i = 0; i < 8; i++) {
            int alpha = Math.max(0, 20 - i * 3); // Decreasing alpha
            g2d.setColor(new Color(0, 0, 0, alpha));
            g2d.fillRect(0, bandY - i, w, 1);
        }
        
        // Draw subtle highlight line at the top of the band
        g2d.setColor(HIGHLIGHT_LINE);
        g2d.fillRect(0, bandY, w, 1);
        
        // Restore original clip
        g2d.setClip(originalClip);
        
        g2d.dispose();
    }
}
