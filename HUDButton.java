package bj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom HUD button with pill-shaped styling that blends with the felt table.
 * Features soft shadows, hover effects, and disabled states.
 */
public class HUDButton extends JButton {
    
    private static final Color NORMAL_COLOR = new Color(0, 0, 0, 180);
    private static final Color HOVER_COLOR = new Color(40, 40, 40, 200);
    private static final Color PRESSED_COLOR = new Color(0, 0, 0, 220);
    private static final Color DISABLED_COLOR = new Color(100, 100, 100, 120);
    private static final Color TEXT_COLOR = new Color(255, 255, 255, 220);
    private static final Color DISABLED_TEXT_COLOR = new Color(200, 200, 200, 120);
    
    private boolean isHovered = false;
    private boolean isPressed = false;
    
    public HUDButton(String text) {
        super(text);
        setupButton();
    }
    
    private void setupButton() {
        // Remove default L&F artifacts
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        
        // Set cursor and font
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        
        // Add mouse listeners for hover effects
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                isHovered = false;
                repaint();
            }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(18, height / 2);
        
        // Determine button color based on state
        Color buttonColor;
        Color textColor;
        
        if (!isEnabled()) {
            buttonColor = DISABLED_COLOR;
            textColor = DISABLED_TEXT_COLOR;
        } else if (isPressed) {
            buttonColor = PRESSED_COLOR;
            textColor = TEXT_COLOR;
        } else if (isHovered) {
            buttonColor = HOVER_COLOR;
            textColor = TEXT_COLOR;
        } else {
            buttonColor = NORMAL_COLOR;
            textColor = TEXT_COLOR;
        }
        
        // Draw shadow (subtle drop shadow)
        if (isEnabled()) {
            g2d.setColor(new Color(0, 0, 0, 60));
            RoundRectangle2D shadow = new RoundRectangle2D.Float(2, 3, width - 4, height - 4, radius, radius);
            g2d.fill(shadow);
        }
        
        // Draw main button
        g2d.setColor(buttonColor);
        RoundRectangle2D button = new RoundRectangle2D.Float(0, 0, width - 2, height - 2, radius, radius);
        g2d.fill(button);
        
        // Draw subtle inner glow for enabled buttons
        if (isEnabled()) {
            g2d.setColor(new Color(255, 255, 255, 30));
            RoundRectangle2D glow = new RoundRectangle2D.Float(1, 1, width - 4, height - 4, radius - 1, radius - 1);
            g2d.fill(glow);
        }
        
        // Draw border
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(button);
        
        // Draw text
        g2d.setColor(textColor);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (width - fm.stringWidth(getText())) / 2;
        int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(getText(), textX, textY);
        
        g2d.dispose();
    }
    
    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int width = Math.max(120, fm.stringWidth(getText()) + 24);
        int height = Math.max(36, fm.getHeight() + 12);
        return new Dimension(width, height);
    }
}
