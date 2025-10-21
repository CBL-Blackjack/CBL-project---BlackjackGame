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
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        if (panelWidth == 0 || panelHeight == 0) {
            return; // Wait for proper sizing
        }
        
        // Use default right zone positioning
        int rightZoneX = Math.max(panelWidth - 500, 200);
        int rightZoneY = panelHeight - 100;
        int rightZoneW = panelWidth - rightZoneX - 24;
        
        layoutButtons(rightZoneX, rightZoneY, rightZoneW);
    }
    
    /**
     * Layout buttons in the specified right zone
     */
    public void layoutButtons(int rightZoneX, int rightZoneY, int rightZoneW) {
        // Button dimensions
        int buttonWidth = 110;
        int buttonHeight = 38;
        int spacing = 16;
        
        // Main gameplay buttons: [Deal] [Hit] [Stand] [Restart] - positioned on right side
        HUDButton setBalanceBtn = buttons.get("setBalance");
        HUDButton dealBtn = buttons.get("deal");
        HUDButton hitBtn = buttons.get("hit");
        HUDButton standBtn = buttons.get("stand");
        HUDButton restartBtn = buttons.get("restart");
        HUDButton undoBetBtn = buttons.get("undoBet");
        HUDButton clearBetBtn = buttons.get("clearBet");
        HUDButton payoutToggleBtn = buttons.get("payoutToggle");
        
        // Position all buttons in the right zone
        HUDButton[] mainGameButtons = {setBalanceBtn, dealBtn, hitBtn, standBtn, restartBtn};
        int visibleGameButtons = 0;
        for (HUDButton btn : mainGameButtons) {
            if (btn != null) visibleGameButtons++;
        }
        
        // Calculate positioning within right zone
        int totalWidth = visibleGameButtons * buttonWidth + (visibleGameButtons - 1) * spacing;
        int startX = rightZoneX + (rightZoneW - totalWidth) / 2; // Center in right zone
        
        // Position main gameplay buttons
        int currentX = startX;
        for (HUDButton btn : mainGameButtons) {
            if (btn != null) {
                btn.setBounds(currentX, rightZoneY, buttonWidth, buttonHeight);
                currentX += buttonWidth + spacing;
            }
        }
        
        // Position Undo and Clear buttons below main buttons
        if (undoBetBtn != null && clearBetBtn != null) {
            int undoClearY = rightZoneY + buttonHeight + 10;
            int undoX = rightZoneX + 20;
            int clearX = rightZoneX + buttonWidth + spacing + 20;
            
            undoBetBtn.setBounds(undoX, undoClearY, buttonWidth, buttonHeight);
            clearBetBtn.setBounds(clearX, undoClearY, buttonWidth, buttonHeight);
        }
        
        // Position Payout Toggle at top right
        if (payoutToggleBtn != null) {
            int payoutToggleX = getWidth() - buttonWidth - 20;
            int payoutToggleY = 20;
            payoutToggleBtn.setBounds(payoutToggleX, payoutToggleY, buttonWidth, buttonHeight);
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
