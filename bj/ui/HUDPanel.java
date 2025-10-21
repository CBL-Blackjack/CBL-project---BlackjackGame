package bj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Transparent HUD panel that overlays the blackjack table.
 * Manages HUD buttons and keyboard shortcuts.
 */
public class HUDPanel extends JPanel {
    
    private final TablePanel tablePanel;
    private final Map<String, HUDButton> buttons = new HashMap<>();
    private final InputMap inputMap;
    private final ActionMap actionMap;
    
    public HUDPanel(TablePanel tablePanel) {
        this.tablePanel = tablePanel;
        
        // Setup transparent overlay
        setOpaque(false);
        setLayout(null); // Absolute positioning
        
        // Setup keyboard shortcuts
        inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        actionMap = getActionMap();
        
        setupKeyboardShortcuts();
    }
    
    /**
     * Add a HUD button to the panel
     */
    public void addHUDButton(String key, HUDButton button) {
        buttons.put(key, button);
        add(button);
    }
    
    /**
     * Get a HUD button by key
     */
    public HUDButton getHUDButton(String key) {
        return buttons.get(key);
    }
    
    /**
     * Layout all HUD buttons in an arc around the bet center
     */
    public void layoutButtons() {
        if (tablePanel == null) return;
        
        Point betCenter = tablePanel.getBetCenter();
        if (betCenter == null) return;
        
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        // Calculate button dimensions that scale with table size
        int buttonWidth = Math.max(120, (int)(panelWidth * 0.12));
        int buttonHeight = Math.max(36, (int)(panelHeight * 0.06));
        
        // Position buttons in an arc around the bet center
        // Main action buttons: [Place Bet] [Deal] [Hit] [Stand] [Restart]
        // Set Balance button goes to far left bottom
        
        HUDButton setBalanceBtn = buttons.get("setBalance");
        HUDButton placeBetBtn = buttons.get("placeBet");
        HUDButton dealBtn = buttons.get("deal");
        HUDButton hitBtn = buttons.get("hit");
        HUDButton standBtn = buttons.get("stand");
        HUDButton restartBtn = buttons.get("restart");
        HUDButton payoutToggleBtn = buttons.get("payoutToggle");
        
        if (setBalanceBtn != null) {
            // Position Set Balance at far left bottom
            int setBalanceX = 20;
            int setBalanceY = panelHeight - buttonHeight - 20;
            setBalanceBtn.setBounds(setBalanceX, setBalanceY, buttonWidth, buttonHeight);
        }
        
        if (payoutToggleBtn != null) {
            // Position Payout Toggle at far right bottom
            int payoutToggleX = panelWidth - buttonWidth - 20;
            int payoutToggleY = panelHeight - buttonHeight - 20;
            payoutToggleBtn.setBounds(payoutToggleX, payoutToggleY, buttonWidth, buttonHeight);
        }
        
        // Calculate arc positions for main buttons
        int arcCenterY = betCenter.y + 60; // Below the bet circle
        int arcRadius = Math.min(200, panelWidth / 4);
        int arcStartX = betCenter.x - arcRadius;
        int arcEndX = betCenter.x + arcRadius;
        
        // Position buttons in arc
        HUDButton[] mainButtons = {dealBtn, hitBtn, standBtn, restartBtn};
        int buttonSpacing = 16;
        int totalButtonWidth = mainButtons.length * buttonWidth + (mainButtons.length - 1) * buttonSpacing;
        int startX = betCenter.x - totalButtonWidth / 2;
        
        for (int i = 0; i < mainButtons.length; i++) {
            if (mainButtons[i] != null) {
                int x = startX + i * (buttonWidth + buttonSpacing);
                int y = arcCenterY;
                mainButtons[i].setBounds(x, y, buttonWidth, buttonHeight);
            }
        }
        
        // Position Undo and Clear buttons below the main arc
        HUDButton undoBtn = buttons.get("undoBet");
        HUDButton clearBtn = buttons.get("clearBet");
        
        if (undoBtn != null && clearBtn != null) {
            int undoClearY = arcCenterY + buttonHeight + 10;
            int undoX = betCenter.x - buttonWidth - 8;
            int clearX = betCenter.x + 8;
            
            undoBtn.setBounds(undoX, undoClearY, buttonWidth, buttonHeight);
            clearBtn.setBounds(clearX, undoClearY, buttonWidth, buttonHeight);
        }
        
        // If window is too small, stack buttons in two rows
        if (panelWidth < 800 || totalButtonWidth > panelWidth - 40) {
            layoutButtonsTwoRows(betCenter, buttonWidth, buttonHeight);
        }
    }
    
    /**
     * Layout buttons in two rows when space is limited
     */
    private void layoutButtonsTwoRows(Point betCenter, int buttonWidth, int buttonHeight) {
        HUDButton placeBetBtn = buttons.get("placeBet");
        HUDButton dealBtn = buttons.get("deal");
        HUDButton hitBtn = buttons.get("hit");
        HUDButton standBtn = buttons.get("stand");
        HUDButton restartBtn = buttons.get("restart");
        
        int spacing = 16;
        int row1Y = betCenter.y + 40;
        int row2Y = betCenter.y + 80;
        
        // Row 1: Place Bet, Deal, Hit
        HUDButton[] row1 = {placeBetBtn, dealBtn, hitBtn};
        int row1TotalWidth = row1.length * buttonWidth + (row1.length - 1) * spacing;
        int row1StartX = betCenter.x - row1TotalWidth / 2;
        
        for (int i = 0; i < row1.length; i++) {
            if (row1[i] != null) {
                int x = row1StartX + i * (buttonWidth + spacing);
                row1[i].setBounds(x, row1Y, buttonWidth, buttonHeight);
            }
        }
        
        // Row 2: Stand, Restart
        HUDButton[] row2 = {standBtn, restartBtn};
        int row2TotalWidth = row2.length * buttonWidth + (row2.length - 1) * spacing;
        int row2StartX = betCenter.x - row2TotalWidth / 2;
        
        for (int i = 0; i < row2.length; i++) {
            if (row2[i] != null) {
                int x = row2StartX + i * (buttonWidth + spacing);
                row2[i].setBounds(x, row2Y, buttonWidth, buttonHeight);
            }
        }
    }
    
    /**
     * Setup keyboard shortcuts
     */
    private void setupKeyboardShortcuts() {
        // B: Place Bet
        inputMap.put(KeyStroke.getKeyStroke("B"), "placeBet");
        actionMap.put("placeBet", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HUDButton btn = buttons.get("placeBet");
                if (btn != null && btn.isEnabled()) {
                    btn.doClick();
                }
            }
        });
        
        // D: Deal
        inputMap.put(KeyStroke.getKeyStroke("D"), "deal");
        actionMap.put("deal", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HUDButton btn = buttons.get("deal");
                if (btn != null && btn.isEnabled()) {
                    btn.doClick();
                }
            }
        });
        
        // H: Hit
        inputMap.put(KeyStroke.getKeyStroke("H"), "hit");
        actionMap.put("hit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HUDButton btn = buttons.get("hit");
                if (btn != null && btn.isEnabled()) {
                    btn.doClick();
                }
            }
        });
        
        // S: Stand
        inputMap.put(KeyStroke.getKeyStroke("S"), "stand");
        actionMap.put("stand", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HUDButton btn = buttons.get("stand");
                if (btn != null && btn.isEnabled()) {
                    btn.doClick();
                }
            }
        });
        
        // R: Restart
        inputMap.put(KeyStroke.getKeyStroke("R"), "restart");
        actionMap.put("restart", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HUDButton btn = buttons.get("restart");
                if (btn != null && btn.isEnabled()) {
                    btn.doClick();
                }
            }
        });
        
        // Ctrl+L (or Cmd+L on Mac): Set Balance
        KeyStroke setBalanceKey = KeyStroke.getKeyStroke("ctrl L");
        inputMap.put(setBalanceKey, "setBalance");
        actionMap.put("setBalance", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HUDButton btn = buttons.get("setBalance");
                if (btn != null && btn.isEnabled()) {
                    btn.doClick();
                }
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Transparent - no background painting
    }
}
