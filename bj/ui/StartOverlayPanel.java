package bj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.RoundRectangle2D;

/**
 * Start overlay panel that shows on app launch with title and start buttons.
 * Features fade-in/out animation and custom balance entry step.
 */
public class StartOverlayPanel extends JPanel implements KeyListener {
    
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 180);
    private static final Color TITLE_COLOR = new Color(255, 255, 255, 240);
    private static final Color SUBTITLE_COLOR = new Color(200, 200, 200, 200);
    private static final Color BUTTON_COLOR = new Color(40, 40, 40, 220);
    private static final Color BUTTON_HOVER_COLOR = new Color(80, 80, 80, 240);
    private static final Color BUTTON_PRESSED_COLOR = new Color(20, 20, 20, 240);
    private static final Color BUTTON_TEXT_COLOR = new Color(255, 255, 255, 240);
    
    private float alpha = 0.0f;
    private boolean fadingIn = true;
    private boolean fadingOut = false;
    private Timer fadeTimer;
    
    private boolean startButtonHovered = false;
    private boolean quickStartButtonHovered = false;
    private boolean helpButtonHovered = false;
    private boolean startButtonPressed = false;
    private boolean quickStartButtonPressed = false;
    private boolean helpButtonPressed = false;
    
    private JFrame parentFrame;
    private Runnable onStartCallback;
    private Runnable onQuickStartCallback;
    private Runnable onHelpCallback;
    
    public StartOverlayPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setupPanel();
        setupKeyboardShortcuts();
        startFadeIn();
    }
    
    private void setupPanel() {
        setOpaque(false);
        setFocusable(true);
        setLayout(null);
        
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
        
        // Set up action map for keyboard shortcuts
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        
        actionMap.put("start", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onStartCallback != null) {
                    onStartCallback.run();
                }
            }
        });
        
        actionMap.put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ESC ignored unless balance already set (not implemented in this overlay)
            }
        });
    }
    
    private void startFadeIn() {
        fadingIn = true;
        fadingOut = false;
        alpha = 0.0f;
        
        fadeTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fadingIn) {
                    alpha += 0.05f;
                    if (alpha >= 1.0f) {
                        alpha = 1.0f;
                        fadingIn = false;
                        fadeTimer.stop();
                    }
                    repaint();
                } else if (fadingOut) {
                    alpha -= 0.05f;
                    if (alpha <= 0.0f) {
                        alpha = 0.0f;
                        fadingOut = false;
                        fadeTimer.stop();
                        setVisible(false);
                    }
                    repaint();
                }
            }
        });
        fadeTimer.start();
    }
    
    public void startFadeOut() {
        fadingOut = true;
        fadingIn = false;
        fadeTimer.start();
    }
    
    private void handleMouseMove(int x, int y) {
        resetButtonStates();
        
        // Check which button is hovered
        Rectangle startButton = getStartButtonBounds();
        Rectangle quickStartButton = getQuickStartButtonBounds();
        Rectangle helpButton = getHelpButtonBounds();
        
        if (startButton.contains(x, y)) {
            startButtonHovered = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (quickStartButton.contains(x, y)) {
            quickStartButtonHovered = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (helpButton.contains(x, y)) {
            helpButtonHovered = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
        
        repaint();
    }
    
    private void handleMousePress(int x, int y) {
        Rectangle startButton = getStartButtonBounds();
        Rectangle quickStartButton = getQuickStartButtonBounds();
        Rectangle helpButton = getHelpButtonBounds();
        
        if (startButton.contains(x, y)) {
            startButtonPressed = true;
        } else if (quickStartButton.contains(x, y)) {
            quickStartButtonPressed = true;
        } else if (helpButton.contains(x, y)) {
            helpButtonPressed = true;
        }
        
        repaint();
    }
    
    private void handleMouseRelease(int x, int y) {
        Rectangle startButton = getStartButtonBounds();
        Rectangle quickStartButton = getQuickStartButtonBounds();
        Rectangle helpButton = getHelpButtonBounds();
        
        if (startButton.contains(x, y) && startButtonPressed) {
            if (onStartCallback != null) {
                onStartCallback.run();
            }
        } else if (quickStartButton.contains(x, y) && quickStartButtonPressed) {
            if (onQuickStartCallback != null) {
                onQuickStartCallback.run();
            }
        } else if (helpButton.contains(x, y) && helpButtonPressed) {
            if (onHelpCallback != null) {
                onHelpCallback.run();
            }
        }
        
        resetButtonStates();
        repaint();
    }
    
    private void resetButtonStates() {
        startButtonHovered = false;
        quickStartButtonHovered = false;
        helpButtonHovered = false;
        startButtonPressed = false;
        quickStartButtonPressed = false;
        helpButtonPressed = false;
    }
    
    private Rectangle getStartButtonBounds() {
        int width = getWidth();
        int height = getHeight();
        int buttonWidth = 200;
        int buttonHeight = 50;
        int x = (width - buttonWidth) / 2;
        int y = height / 2 + 20;
        return new Rectangle(x, y, buttonWidth, buttonHeight);
    }
    
    private Rectangle getQuickStartButtonBounds() {
        int width = getWidth();
        int height = getHeight();
        int buttonWidth = 200;
        int buttonHeight = 50;
        int x = (width - buttonWidth) / 2;
        int y = height / 2 + 80;
        return new Rectangle(x, y, buttonWidth, buttonHeight);
    }
    
    private Rectangle getHelpButtonBounds() {
        int width = getWidth();
        int height = getHeight();
        int buttonWidth = 120;
        int buttonHeight = 30;
        int x = (width - buttonWidth) / 2;
        int y = height / 2 + 140;
        return new Rectangle(x, y, buttonWidth, buttonHeight);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Apply alpha composite
        g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw semi-transparent overlay
        g2d.setColor(OVERLAY_COLOR);
        g2d.fillRect(0, 0, width, height);
        
        // Draw title
        g2d.setColor(TITLE_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics titleMetrics = g2d.getFontMetrics();
        String title = "BLACKJACK";
        int titleX = (width - titleMetrics.stringWidth(title)) / 2;
        int titleY = height / 2 - 80;
        g2d.drawString(title, titleX, titleY);
        
        // Draw subtitle
        g2d.setColor(SUBTITLE_COLOR);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics subtitleMetrics = g2d.getFontMetrics();
        String subtitle = "Press Start to play";
        int subtitleX = (width - subtitleMetrics.stringWidth(subtitle)) / 2;
        int subtitleY = height / 2 - 40;
        g2d.drawString(subtitle, subtitleX, subtitleY);
        
        // Draw buttons
        drawButton(g2d, getStartButtonBounds(), "Start", 
                  startButtonHovered, startButtonPressed);
        drawButton(g2d, getQuickStartButtonBounds(), "Quick Start (Balance 100)", 
                  quickStartButtonHovered, quickStartButtonPressed);
        drawButton(g2d, getHelpButtonBounds(), "Help / Rules", 
                  helpButtonHovered, helpButtonPressed);
        
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
        g2d.setColor(new Color(0, 0, 0, 100));
        RoundRectangle2D shadow = new RoundRectangle2D.Float(
            bounds.x + 2, bounds.y + 3, bounds.width - 2, bounds.height - 2, 
            bounds.height / 2, bounds.height / 2);
        g2d.fill(shadow);
        
        // Draw button
        g2d.setColor(buttonColor);
        RoundRectangle2D button = new RoundRectangle2D.Float(
            bounds.x, bounds.y, bounds.width - 2, bounds.height - 2, 
            bounds.height / 2, bounds.height / 2);
        g2d.fill(button);
        
        // Draw button border
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(button);
        
        // Draw button text
        g2d.setColor(BUTTON_TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int textY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
    
    // Setters for callbacks
    public void setOnStartCallback(Runnable callback) {
        this.onStartCallback = callback;
    }
    
    public void setOnQuickStartCallback(Runnable callback) {
        this.onQuickStartCallback = callback;
    }
    
    public void setOnHelpCallback(Runnable callback) {
        this.onHelpCallback = callback;
    }
    
    // KeyListener implementation
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyPressed(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
}
