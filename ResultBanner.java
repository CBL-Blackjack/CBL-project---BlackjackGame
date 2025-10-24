package bj.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A result banner overlay that displays game outcomes.
 * Auto-dismisses after a delay or when the user clicks OK.
 */
public class ResultBanner extends JPanel {
    
    private JLabel messageLabel;
    private JButton okButton;
    private Timer autoDismissTimer;
    private boolean isVisible = false;
    private String resultText = "";
    
    // Colors for the banner
    private static final Color BANNER_BG = new Color(0, 0, 0, 200);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BUTTON_BG = new Color(70, 130, 180);
    
    public ResultBanner() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setVisible(false);
        setEnabled(false); // Don't consume events outside the banner area
        
        // Temporary diagnostic border
        setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        
        // Create message label
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        messageLabel.setForeground(TEXT_COLOR);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        
        // Create OK button
        okButton = new JButton("OK");
        okButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        okButton.setBackground(BUTTON_BG);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        okButton.addActionListener(e -> dismiss());
        
        // Add components
        add(messageLabel, BorderLayout.CENTER);
        add(okButton, BorderLayout.SOUTH);
        
        // Auto-dismiss timer
        autoDismissTimer = new Timer(2000, e -> dismiss());
    }
    
    /**
     * Set the result text
     */
    public void setResultText(String text) {
        this.resultText = text;
        messageLabel.setText(text);
    }
    
    /**
     * Show the result banner with the given message
     * @param message The outcome message to display
     */
    public void showResult(String message) {
        setResultText(message);
        setVisible(true);
        isVisible = true;
        setEnabled(true); // Enable only when showing
        
        // Start auto-dismiss timer
        autoDismissTimer.stop();
        autoDismissTimer.start();
        
        // Center the banner
        SwingUtilities.invokeLater(() -> {
            if (getParent() != null) {
                Dimension parentSize = getParent().getSize();
                Dimension bannerSize = getPreferredSize();
                int x = (parentSize.width - bannerSize.width) / 2;
                int y = (parentSize.height - bannerSize.height) / 2;
                setBounds(x, y, bannerSize.width, bannerSize.height);
            }
        });
    }
    
    /**
     * Dismiss the banner
     */
    public void dismiss() {
        autoDismissTimer.stop();
        setVisible(false);
        isVisible = false;
        setEnabled(false); // Disable when hidden
    }
    
    /**
     * Check if the banner is currently visible
     * @return true if visible
     */
    public boolean isBannerVisible() {
        return isVisible;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (!isVisible) return;
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Only paint the banner area - rest is completely transparent
        Dimension size = getPreferredSize();
        int x = (getWidth() - size.width) / 2;
        int y = (getHeight() - size.height) / 2;
        
        // Draw banner background with rounded corners
        g2d.setColor(BANNER_BG);
        int arc = 15;
        g2d.fillRoundRect(x, y, size.width, size.height, arc, arc);
        
        // Draw border
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(x + 1, y + 1, size.width - 2, size.height - 2, arc, arc);
        
        // Draw diagnostic outline (temporary)
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRect(x, y, size.width, size.height);
        
        g2d.dispose();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 120);
    }
}
