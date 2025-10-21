package bj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom balance entry panel that appears as a modal overlay.
 * Features validation and smooth integration with the start overlay.
 */
public class BalanceEntryPanel extends JPanel implements KeyListener {
    
    private static final Color PANEL_COLOR = new Color(30, 30, 30, 240);
    private static final Color TITLE_COLOR = new Color(255, 255, 255, 240);
    private static final Color BUTTON_COLOR = new Color(40, 40, 40, 220);
    private static final Color BUTTON_HOVER_COLOR = new Color(80, 80, 80, 240);
    private static final Color BUTTON_PRESSED_COLOR = new Color(20, 20, 20, 240);
    private static final Color BUTTON_TEXT_COLOR = new Color(255, 255, 255, 240);
    private static final Color ERROR_COLOR = new Color(255, 100, 100, 240);
    private static final Color TEXT_FIELD_COLOR = new Color(50, 50, 50, 255);
    private static final Color TEXT_FIELD_BORDER_COLOR = new Color(100, 100, 100, 200);
    
    private JTextField balanceField;
    private JLabel errorLabel;
    private boolean continueButtonHovered = false;
    private boolean cancelButtonHovered = false;
    private boolean continueButtonPressed = false;
    private boolean cancelButtonPressed = false;
    
    private Runnable onContinueCallback;
    private Runnable onCancelCallback;
    
    public BalanceEntryPanel() {
        setupPanel();
        setupKeyboardShortcuts();
    }
    
    private void setupPanel() {
        setOpaque(false);
        setFocusable(true);
        setLayout(null);
        
        // Create text field
        balanceField = new JTextField();
        balanceField.setFont(new Font("Arial", Font.PLAIN, 18));
        balanceField.setHorizontalAlignment(JTextField.CENTER);
        balanceField.setOpaque(true);
        balanceField.setBackground(TEXT_FIELD_COLOR);
        balanceField.setForeground(Color.WHITE);
        balanceField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        balanceField.setCaretColor(Color.WHITE);
        
        // Add key listener to text field for validation
        balanceField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleContinue();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    handleCancel();
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        // Create error label
        errorLabel = new JLabel();
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        
        add(balanceField);
        add(errorLabel);
        
        // Add mouse listeners for button interactions
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                handleMousePress(e.getX(), e.getY());
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                handleMouseRelease(e.getX(), e.getY());
            }
            
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                resetButtonStates();
            }
        });
        
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
        });
    }
    
    private void setupKeyboardShortcuts() {
        addKeyListener(this);
    }
    
    private void handleMouseMove(int x, int y) {
        resetButtonStates();
        
        Rectangle continueButton = getContinueButtonBounds();
        Rectangle cancelButton = getCancelButtonBounds();
        
        if (continueButton.contains(x, y)) {
            continueButtonHovered = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (cancelButton.contains(x, y)) {
            cancelButtonHovered = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
        
        repaint();
    }
    
    private void handleMousePress(int x, int y) {
        Rectangle continueButton = getContinueButtonBounds();
        Rectangle cancelButton = getCancelButtonBounds();
        
        if (continueButton.contains(x, y)) {
            continueButtonPressed = true;
        } else if (cancelButton.contains(x, y)) {
            cancelButtonPressed = true;
        }
        
        repaint();
    }
    
    private void handleMouseRelease(int x, int y) {
        Rectangle continueButton = getContinueButtonBounds();
        Rectangle cancelButton = getCancelButtonBounds();
        
        if (continueButton.contains(x, y) && continueButtonPressed) {
            handleContinue();
        } else if (cancelButton.contains(x, y) && cancelButtonPressed) {
            handleCancel();
        }
        
        resetButtonStates();
        repaint();
    }
    
    private void resetButtonStates() {
        continueButtonHovered = false;
        cancelButtonHovered = false;
        continueButtonPressed = false;
        cancelButtonPressed = false;
    }
    
    private void handleContinue() {
        String input = balanceField.getText().trim();
        if (input.isEmpty()) {
            showError("Please enter a balance amount");
            return;
        }
        
        try {
            int amount = Integer.parseInt(input);
            if (amount <= 0) {
                showError("Balance must be greater than 0");
                return;
            }
            if (amount > Integer.MAX_VALUE) {
                showError("Balance amount too large");
                return;
            }
            
            clearError();
            if (onContinueCallback != null) {
                onContinueCallback.run();
            }
        } catch (NumberFormatException e) {
            showError("Invalid number format");
        }
    }
    
    private void handleCancel() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        repaint();
    }
    
    private void clearError() {
        errorLabel.setText("");
        repaint();
    }
    
    public int getBalanceAmount() {
        try {
            return Integer.parseInt(balanceField.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public void focusTextField() {
        SwingUtilities.invokeLater(() -> {
            balanceField.requestFocusInWindow();
            balanceField.selectAll();
        });
    }
    
    private Rectangle getContinueButtonBounds() {
        int width = getWidth();
        int height = getHeight();
        int buttonWidth = 120;
        int buttonHeight = 40;
        int x = width / 2 - buttonWidth - 10;
        int y = height - 80;
        return new Rectangle(x, y, buttonWidth, buttonHeight);
    }
    
    private Rectangle getCancelButtonBounds() {
        int width = getWidth();
        int height = getHeight();
        int buttonWidth = 120;
        int buttonHeight = 40;
        int x = width / 2 + 10;
        int y = height - 80;
        return new Rectangle(x, y, buttonWidth, buttonHeight);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Calculate panel bounds (centered)
        int panelWidth = 400;
        int panelHeight = 250;
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight) / 2;
        
        // Draw panel shadow
        g2d.setColor(new Color(0, 0, 0, 100));
        RoundRectangle2D shadow = new RoundRectangle2D.Float(
            panelX + 3, panelY + 3, panelWidth, panelHeight, 20, 20);
        g2d.fill(shadow);
        
        // Draw panel background
        g2d.setColor(PANEL_COLOR);
        RoundRectangle2D panel = new RoundRectangle2D.Float(
            panelX, panelY, panelWidth, panelHeight, 20, 20);
        g2d.fill(panel);
        
        // Draw panel border
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(panel);
        
        // Draw title
        g2d.setColor(TITLE_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics titleMetrics = g2d.getFontMetrics();
        String title = "Set Balance";
        int titleX = panelX + (panelWidth - titleMetrics.stringWidth(title)) / 2;
        int titleY = panelY + 40;
        g2d.drawString(title, titleX, titleY);
        
        // Position text field
        int fieldWidth = 200;
        int fieldHeight = 35;
        int fieldX = panelX + (panelWidth - fieldWidth) / 2;
        int fieldY = panelY + 80;
        balanceField.setBounds(fieldX, fieldY, fieldWidth, fieldHeight);
        
        // Position error label
        int errorY = fieldY + fieldHeight + 10;
        errorLabel.setBounds(panelX + 20, errorY, panelWidth - 40, 20);
        
        // Draw buttons
        drawButton(g2d, getContinueButtonBounds(), "Continue", 
                  continueButtonHovered, continueButtonPressed);
        drawButton(g2d, getCancelButtonBounds(), "Cancel", 
                  cancelButtonHovered, cancelButtonPressed);
        
        g2d.dispose();
    }
    
    private void drawButton(Graphics2D g2d, Rectangle bounds, String text, 
                           boolean hovered, boolean pressed) {
        Color buttonColor = BUTTON_COLOR;
        if (pressed) {
            buttonColor = BUTTON_PRESSED_COLOR;
        } else if (hovered) {
            buttonColor = BUTTON_HOVER_COLOR;
        }
        
        // Draw button shadow
        g2d.setColor(new Color(0, 0, 0, 80));
        RoundRectangle2D shadow = new RoundRectangle2D.Float(
            bounds.x + 1, bounds.y + 2, bounds.width - 1, bounds.height - 1, 
            bounds.height / 2, bounds.height / 2);
        g2d.fill(shadow);
        
        // Draw button
        g2d.setColor(buttonColor);
        RoundRectangle2D button = new RoundRectangle2D.Float(
            bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 
            bounds.height / 2, bounds.height / 2);
        g2d.fill(button);
        
        // Draw button border
        g2d.setColor(new Color(255, 255, 255, 50));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(button);
        
        // Draw button text
        g2d.setColor(BUTTON_TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int textY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
    
    // Setters for callbacks
    public void setOnContinueCallback(Runnable callback) {
        this.onContinueCallback = callback;
    }
    
    public void setOnCancelCallback(Runnable callback) {
        this.onCancelCallback = callback;
    }
    
    // KeyListener implementation
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyPressed(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
}
