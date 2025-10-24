package bj.ui.bet;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;

/**
 * Custom button for chip rack that displays a mini chip with denomination.
 * Supports click and drag interactions.
 */
public class RackChipButton extends JButton {
    
    private static final Color BUTTON_COLOR = new Color(40, 40, 40, 200);
    private static final Color BUTTON_HOVER_COLOR = new Color(60, 60, 60, 220);
    private static final Color BUTTON_PRESSED_COLOR = new Color(20, 20, 20, 240);
    private static final Color TEXT_COLOR = new Color(255, 255, 255, 240);
    
    private final int denomination;
    private final Color chipColor;
    private final int chipRadius;
    private boolean isHovered = false;
    private boolean isPressed = false;
    
    public RackChipButton(int denomination) {
        super(String.valueOf(denomination));
        this.denomination = denomination;
        this.chipColor = BetManager.getColorForDenomination(denomination);
        this.chipRadius = BetManager.getRadiusForDenomination(denomination);
        
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
        setFont(getFont().deriveFont(Font.BOLD, 12f));
        
        // Set preferred size
        setPreferredSize(new Dimension(60, 60));
        
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
        int radius = Math.min(15, height / 2);
        
        // Determine button color based on state
        Color buttonColor;
        Color textColor;
        
        if (!isEnabled()) {
            buttonColor = new Color(100, 100, 100, 120);
            textColor = new Color(150, 150, 150, 120);
        } else if (isPressed) {
            buttonColor = BUTTON_PRESSED_COLOR;
            textColor = TEXT_COLOR;
        } else if (isHovered) {
            buttonColor = BUTTON_HOVER_COLOR;
            textColor = TEXT_COLOR;
        } else {
            buttonColor = BUTTON_COLOR;
            textColor = TEXT_COLOR;
        }
        
        // Draw button shadow
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
            g2d.setColor(new Color(255, 255, 255, 20));
            RoundRectangle2D glow = new RoundRectangle2D.Float(1, 1, width - 4, height - 4, radius - 1, radius - 1);
            g2d.fill(glow);
        }
        
        // Draw border
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(button);
        
        // Draw mini chip
        drawMiniChip(g2d, width / 2, height / 2 - 5);
        
        // Draw denomination text below chip
        g2d.setColor(textColor);
        g2d.setFont(getFont());
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(denomination);
        int textX = (width - fm.stringWidth(text)) / 2;
        int textY = height - 8;
        g2d.drawString(text, textX, textY);
        
        g2d.dispose();
    }
    
    /**
     * Draw a small representation of the chip
     */
    private void drawMiniChip(Graphics2D g2d, int centerX, int centerY) {
        int miniRadius = chipRadius / 3;
        
        // Draw chip shadow
        g2d.setColor(new Color(0, 0, 0, 80));
        Ellipse2D shadow = new Ellipse2D.Float(centerX - miniRadius + 1, centerY - miniRadius + 2, 
                                             miniRadius * 2, miniRadius * 2);
        g2d.fill(shadow);
        
        // Draw chip body
        g2d.setColor(chipColor);
        Ellipse2D chip = new Ellipse2D.Float(centerX - miniRadius, centerY - miniRadius, 
                                           miniRadius * 2, miniRadius * 2);
        g2d.fill(chip);
        
        // Draw white ring
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(chip);
        
        // Draw denomination in center
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(8, miniRadius)));
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(denomination);
        int textX = centerX - fm.stringWidth(text) / 2;
        int textY = centerY + fm.getAscent() / 2 - 1;
        g2d.drawString(text, textX, textY);
    }
    
    public int getDenomination() {
        return denomination;
    }
    
    public Color getChipColor() {
        return chipColor;
    }
}
