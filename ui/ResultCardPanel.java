package bj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * A minimal, theme-matching result card that shows game outcomes.
 * Transparent outside its rounded rectangle, modeless, and non-blocking.
 */
public class ResultCardPanel extends JPanel {
    
    private final ResultCardModel model;
    private JButton newRoundBtn;
    private JButton leaveTableBtn;
    
    // Colors matching casino theme
    private static final Color CARD_BG = new Color(20, 40, 20); // Dark green
    private static final Color CARD_BORDER = new Color(60, 120, 60); // Lighter green
    private static final Color TEXT_COLOR = new Color(220, 220, 200); // Off-white
    private static final Color WIN_COLOR = new Color(100, 200, 100); // Green for wins
    private static final Color LOSS_COLOR = new Color(200, 100, 100); // Red for losses
    private static final Color PUSH_COLOR = new Color(150, 150, 150); // Gray for push
    private static final Color BUTTON_BG = new Color(40, 80, 40);
    private static final Color BUTTON_HOVER = new Color(60, 120, 60);
    
    // Debug flag - set to false to disable all debug output
    private static final boolean DEBUG = false;
    
    public ResultCardPanel() {
        this.model = new ResultCardModel();
        setOpaque(false);
        setLayout(null); // Use absolute positioning for custom painting
        setVisible(false);
        setEnabled(false); // Don't consume events outside card area
        
        // Create buttons
        newRoundBtn = createButton("New Round");
        leaveTableBtn = createButton("Leave Table");
        
        // Position buttons (will be set in paintComponent)
        add(newRoundBtn);
        add(leaveTableBtn);
    }
    
    /**
     * Create a styled button
     */
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(BUTTON_BG);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setPreferredSize(new Dimension(120, 35));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(BUTTON_HOVER);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(BUTTON_BG);
            }
        });
        
        return button;
    }
    
    /**
     * Set button action listeners
     */
    public void setNewRoundAction(ActionListener listener) {
        newRoundBtn.addActionListener(listener);
    }
    
    public void setLeaveTableAction(ActionListener listener) {
        leaveTableBtn.addActionListener(listener);
    }
    
    /**
     * Show the result card with outcome information (EDT-safe)
     */
    public void showResultCard(String title, int netChange, int balance, ResultCardModel.Theme theme) {
        // Set model data
        model.setCardData(title, netChange, balance, theme);
        model.setVisible(true);
        
        // Update UI on EDT
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            setEnabled(true);
            updatePosition();
            revalidate();
            repaint();
            
            // Debug log
            if (DEBUG) {
                System.out.println("ResultCardPanel.showResultCard called on EDT: " + 
                                 Thread.currentThread().getName());
            }
        });
    }
    
    /**
     * Hide the result card (EDT-safe)
     */
    public void hideResultCard() {
        model.setVisible(false);
        
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            setEnabled(false);
            revalidate();
            repaint();
            
            // Debug log
            if (DEBUG) {
                System.out.println("ResultCardPanel.hideResultCard called on EDT: " + 
                                 Thread.currentThread().getName());
            }
        });
    }
    
    /**
     * Check if the card is visible
     */
    public boolean isCardVisible() {
        return model.isVisible();
    }
    
    /**
     * Update position after resize/fullscreen
     */
    public void updatePosition() {
        if (model.isVisible() && getParent() != null) {
            Dimension parentSize = getParent().getSize();
            Dimension cardSize = getPreferredSize();
            int x = (parentSize.width - cardSize.width) / 2;
            int y = (parentSize.height - cardSize.height) / 2;
            setBounds(x, y, cardSize.width, cardSize.height);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (!model.isVisible()) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Card dimensions
        int cardWidth = 480;
        int cardHeight = 200;
        int x = (getWidth() - cardWidth) / 2;
        int y = (getHeight() - cardHeight) / 2;
        
        // Create clipping path for card area
        RoundRectangle2D cardShape = new RoundRectangle2D.Float(x, y, cardWidth, cardHeight, 24, 24);
        g2d.setClip(cardShape);
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillRoundRect(x + 3, y + 3, cardWidth, cardHeight, 24, 24);
        
        // Draw card background
        g2d.setColor(CARD_BG);
        g2d.fillRoundRect(x, y, cardWidth, cardHeight, 24, 24);
        
        // Draw border
        g2d.setColor(CARD_BORDER);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(x, y, cardWidth, cardHeight, 24, 24);
        
        // Draw inner glow
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(x + 1, y + 1, cardWidth - 2, cardHeight - 2, 24, 24);
        
        // Reset clip for text rendering
        g2d.setClip(null);
        
        // Render text with high contrast colors
        renderText(g2d, x, y, cardWidth, cardHeight);
        
        // Position buttons
        positionButtons(x, y, cardWidth, cardHeight);
        
        g2d.dispose();
    }
    
    /**
     * Render text with theme-aware colors
     */
    private void renderText(Graphics2D g2d, int x, int y, int cardWidth, int cardHeight) {
        // Title font
        Font titleFont = new Font("SansSerif", Font.BOLD, 24);
        g2d.setFont(titleFont);
        FontMetrics titleMetrics = g2d.getFontMetrics();
        
        // Body font
        Font bodyFont = new Font("SansSerif", Font.PLAIN, 16);
        g2d.setFont(bodyFont);
        FontMetrics bodyMetrics = g2d.getFontMetrics();
        
        // Balance font
        Font balanceFont = new Font("SansSerif", Font.PLAIN, 14);
        g2d.setFont(balanceFont);
        FontMetrics balanceMetrics = g2d.getFontMetrics();
        
        // Get theme colors
        Color titleColor = getThemeColor(model.getTheme(), true);
        Color deltaColor = getThemeColor(model.getTheme(), false);
        
        // Title
        String title = model.getTitle();
        int titleX = x + (cardWidth - titleMetrics.stringWidth(title)) / 2;
        int titleY = y + 40;
        g2d.setColor(titleColor);
        g2d.setFont(titleFont);
        g2d.drawString(title, titleX, titleY);
        
        // Delta
        String delta = model.getDeltaFormatted();
        int deltaX = x + (cardWidth - bodyMetrics.stringWidth(delta)) / 2;
        int deltaY = titleY + 35;
        g2d.setColor(deltaColor);
        g2d.setFont(bodyFont);
        g2d.drawString(delta, deltaX, deltaY);
        
        // Balance
        String balance = model.getBalanceFormatted();
        int balanceX = x + (cardWidth - balanceMetrics.stringWidth(balance)) / 2;
        int balanceY = deltaY + 25;
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(balanceFont);
        g2d.drawString(balance, balanceX, balanceY);
        
        // Debug info (only if DEBUG is enabled)
        if (DEBUG) {
            String debugInfo = String.format("Title: %d, Delta: %d, Balance: %d, Theme: %s, Visible: %s",
                title.length(), delta.length(), balance.length(), 
                model.getTheme(), model.isVisible());
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2d.drawString(debugInfo, x + 10, y + cardHeight - 10);
        }
    }
    
    /**
     * Get theme-appropriate colors
     */
    private Color getThemeColor(ResultCardModel.Theme theme, boolean isTitle) {
        switch (theme) {
            case WIN:
                return isTitle ? WIN_COLOR : WIN_COLOR;
            case LOSE:
                return isTitle ? LOSS_COLOR : LOSS_COLOR;
            case BJ:
                return isTitle ? new Color(255, 215, 0) : new Color(255, 215, 0); // Gold
            case PUSH:
            default:
                return isTitle ? TEXT_COLOR : PUSH_COLOR;
        }
    }
    
    /**
     * Position buttons within the card
     */
    private void positionButtons(int x, int y, int cardWidth, int cardHeight) {
        int buttonY = y + cardHeight - 50;
        int buttonWidth = 120;
        int buttonHeight = 35;
        int gap = 15;
        
        int totalButtonWidth = 2 * buttonWidth + gap;
        int startX = x + (cardWidth - totalButtonWidth) / 2;
        
        newRoundBtn.setBounds(startX, buttonY, buttonWidth, buttonHeight);
        leaveTableBtn.setBounds(startX + buttonWidth + gap, buttonY, buttonWidth, buttonHeight);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(480, 200);
    }
    
    @Override
    public boolean contains(int x, int y) {
        if (!model.isVisible()) return false;
        
        // Only respond to clicks inside the card area
        Dimension size = getPreferredSize();
        int cardX = (getWidth() - size.width) / 2;
        int cardY = (getHeight() - size.height) / 2;
        
        return x >= cardX && x < cardX + size.width && 
               y >= cardY && y < cardY + size.height;
    }
}
