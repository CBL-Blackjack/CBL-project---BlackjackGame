package bj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import bj.model.Deck;
import bj.model.Hand;
import bj.model.Card;
import bj.logic.BlackjackEngine;
import bj.logic.GameState;
import bj.ui.bet.BetManager;
import bj.ui.bet.ChipRackPanel;
import bj.ui.bet.ChipSprite;

/**
 * Main application class for the Blackjack game
 */
public class BlackjackApp extends JFrame {
    
    // Game engine
    private BlackjackEngine engine;
    
    // UI components
    private TablePanel tablePanel;
    private TableAnimator animator;
    private JLabel balanceLabel;
    // Legacy control panel removed - now using HUD overlay
    private HUDButton setBalanceBtn, dealBtn, hitBtn, standBtn, restartBtn;
    private HUDButton undoBetBtn, clearBetBtn;
    
    // HUD components
    private HUDPanel hudPanel;
    
    // Start overlay components
    private StartOverlayPanel startOverlay;
    private BalanceEntryPanel balanceEntryPanel;
    
    // Fullscreen support
    private JLayeredPane layeredPane;
    private FullscreenManager fullscreenManager;
    
    // Chip betting components
    private BetManager betManager;
    private ChipRackPanel chipRackPanel;
    
    // Legacy model components (will be replaced by engine)
    private Deck deck;
    private Hand dealerHand;
    private Hand playerHand;
    
    // UI labels for displaying hands
    private JLabel dealerHandLabel;
    private JLabel playerHandLabel;
    
    public BlackjackApp() {
        initializeComponents();
        createAndShowGUI();
    }
    
    /**
     * Initialize the main GUI components
     */
    private void initializeComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Blackjack Game");
        
        // Initialize game engine
        engine = new BlackjackEngine(0);
        
        // Create table panel
        tablePanel = new TablePanel();
        
        // Create animator and associate with table panel
        animator = new TableAnimator(tablePanel);
        tablePanel.setAnimator(animator);
        
        // Create bet manager
        betManager = new BetManager();
        
        // Create balance label
        balanceLabel = new JLabel("Balance: $0");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        balanceLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize game model components
        deck = new Deck();
        dealerHand = new Hand();
        playerHand = new Hand();
        
        // Create hand display labels
        dealerHandLabel = new JLabel("Dealer: ");
        dealerHandLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dealerHandLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        playerHandLabel = new JLabel("Player: ");
        playerHandLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        playerHandLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Control panel removed - now using HUD buttons
    }
    
    /**
     * Create and show the GUI
     */
    public void createAndShowGUI() {
        // Set up main layout
        setLayout(new BorderLayout());
        
        // Create layered pane for HUD overlay
        layeredPane = new JLayeredPane();
        
        // Add table panel to DEFAULT_LAYER
        layeredPane.add(tablePanel, JLayeredPane.DEFAULT_LAYER);
        tablePanel.setBounds(0, 0, 900, 700); // Will be resized by layout
        
        // Create HUD panel for controls
        hudPanel = new HUDPanel(tablePanel);
        layeredPane.add(hudPanel, JLayeredPane.DRAG_LAYER);
        layeredPane.setLayer(hudPanel, JLayeredPane.DRAG_LAYER);
        layeredPane.moveToFront(hudPanel);
        hudPanel.setBounds(0, 0, 900, 700); // Will be resized by layout
        
        // Create start overlay
        startOverlay = new StartOverlayPanel(this);
        // Start overlay is now installed as glass pane, not in layered pane
        
        // Create balance entry panel
        balanceEntryPanel = new BalanceEntryPanel();
        layeredPane.add(balanceEntryPanel, JLayeredPane.DRAG_LAYER);
        balanceEntryPanel.setBounds(0, 0, 900, 700); // Will be resized by layout
        balanceEntryPanel.setVisible(false);
        
        // Create chip rack panel
        chipRackPanel = new ChipRackPanel(tablePanel);
        layeredPane.add(chipRackPanel, JLayeredPane.PALETTE_LAYER);
        // Position at bottom of table with height of 150px
        chipRackPanel.setBounds(0, 550, 900, 150);
        chipRackPanel.setVisible(true);
        chipRackPanel.setEnabledRack(true);
        
        // Create HUD buttons
        createHUDButtons();
        
        // Setup overlay callbacks
        setupOverlayCallbacks();
        
        // Setup chip rack callback
        chipRackPanel.setOnChipPlaceCallback(this::placeChip);
        
        // Add layered pane to center
        add(balanceLabel, BorderLayout.NORTH);
        add(layeredPane, BorderLayout.CENTER);
        
        // Initialize fullscreen manager
        fullscreenManager = new FullscreenManager(this);
        
        // Install start overlay as glass pane for guaranteed full coverage
        getRootPane().setGlassPane(startOverlay);
        startOverlay.setVisible(false);
        startOverlay.setOpaque(false);
        
        // Configure frame
        setPreferredSize(new Dimension(900, 700));
        pack();
        setLocationRelativeTo(null);
        
        // Show the frame
        setVisible(true);
        
        // Setup key bindings for fullscreen
        setupKeyBindings();
        
        // Layout HUD buttons after frame is visible
        SwingUtilities.invokeLater(() -> {
            layoutHUDButtons();
            animator.start();
            
            // Enter fullscreen mode
            fullscreenManager.enterFullscreen();
            
            // Relayout after fullscreen entry
            relayoutAll();
            
            // Show start overlay and disable HUD buttons initially
            startOverlay.setVisible(true);
            startOverlay.setFocusable(true);
            startOverlay.requestFocusInWindow();
            disableHUDButtons();
        });
        
        // Add component listener for resize handling
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                relayoutAll();
            }
        });
    }
    
    /**
     * Create HUD buttons and wire them to action listeners
     */
    private void createHUDButtons() {
        // Create HUD buttons
        setBalanceBtn = new HUDButton("Set Balance");
        dealBtn = new HUDButton("Deal");
        hitBtn = new HUDButton("Hit");
        standBtn = new HUDButton("Stand");
        restartBtn = new HUDButton("Restart");
        undoBetBtn = new HUDButton("Undo Bet");
        clearBetBtn = new HUDButton("Clear Bet");
        
        // Add action listeners (reuse existing methods)
        setBalanceBtn.addActionListener(e -> setBalance());
        dealBtn.addActionListener(e -> deal());
        hitBtn.addActionListener(e -> hit());
        standBtn.addActionListener(e -> stand());
        restartBtn.addActionListener(e -> restart());
        undoBetBtn.addActionListener(e -> undoLastChip());
        clearBetBtn.addActionListener(e -> clearAllChips());
        
        // Add payout toggle button
        HUDButton payoutToggleBtn = new HUDButton("Payout: 3:2");
        payoutToggleBtn.addActionListener(e -> {
            if (engine.getPayout() == bj.logic.BlackjackPayout.THREE_TO_TWO) {
                engine.setPayout(bj.logic.BlackjackPayout.SIX_TO_FIVE);
                payoutToggleBtn.setText("Payout: 6:5");
            } else {
                engine.setPayout(bj.logic.BlackjackPayout.THREE_TO_TWO);
                payoutToggleBtn.setText("Payout: 3:2");
            }
        });
        
        // Add buttons to HUD panel
        hudPanel.addHUDButton("setBalance", setBalanceBtn);
        hudPanel.addHUDButton("deal", dealBtn);
        hudPanel.addHUDButton("hit", hitBtn);
        hudPanel.addHUDButton("stand", standBtn);
        hudPanel.addHUDButton("restart", restartBtn);
        hudPanel.addHUDButton("undoBet", undoBetBtn);
        hudPanel.addHUDButton("clearBet", clearBetBtn);
        hudPanel.addHUDButton("payoutToggle", payoutToggleBtn);
    }
    
    /**
     * Setup key bindings for fullscreen toggle
     */
    private void setupKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        
        // F11 to toggle fullscreen
        inputMap.put(KeyStroke.getKeyStroke("F11"), "toggleFullscreen");
        actionMap.put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fullscreenManager.toggleFullscreen();
            }
        });
        
        // ESC to exit fullscreen
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "exitFullscreen");
        actionMap.put("exitFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fullscreenManager.isFullscreen()) {
                    fullscreenManager.exitFullscreen();
                }
            }
        });
    }
    
    /**
     * Layout HUD buttons based on current panel size
     */
    private void layoutHUDButtons() {
        if (hudPanel != null) {
            hudPanel.layoutButtons();
        }
    }
    
    /**
     * Layout HUD buttons in the right zone
     */
    private void layoutHUDButtons(int rightZoneX, int rightZoneY, int rightZoneW) {
        if (hudPanel != null) {
            hudPanel.layoutButtons(rightZoneX, rightZoneY, rightZoneW);
        }
    }
    
    /**
     * Relayout all components based on current window size with safe zones
     */
    public void relayoutAll() {
        // Get content pane size
        Dimension sz = getContentPane().getSize();
        int w = sz.width;
        int h = sz.height;
        
        // Set JLayeredPane to fill content pane
        layeredPane.setBounds(0, 0, w, h);
        
        // Set all panel bounds to fill the layered pane
        tablePanel.setBounds(0, 0, w, h);
        
        // Ensure HUD panel is properly configured and visible
        hudPanel.setOpaque(false);
        hudPanel.setLayout(null); // absolute positioning only
        hudPanel.setBounds(0, 0, w, h); // fill the whole layeredPane
        hudPanel.setVisible(true);
        
        chipRackPanel.setBounds(0, 0, w, h);
        
        // Resize overlay to cover entire content area
        if (startOverlay != null) {
            startOverlay.setBounds(0, 0, w, h);
        }
        if (balanceEntryPanel != null) {
            balanceEntryPanel.setBounds(0, 0, w, h);
        }
        
        // Layout HUD and chip tray with proper constraints
        layoutHUD(w, h);
        layoutChipTray(w, h);
        
        // Revalidate and repaint
        layeredPane.revalidate();
        layeredPane.repaint();
    }
    
    /**
     * Layout HUD buttons with proper constraints and compact modes
     */
    private void layoutHUD(int w, int h) {
        // Safe bottom band constants
        final int BTN_W = 112;
        final int BTN_H = 38;
        final int GAP = 14;
        final int MARGIN_R = 32;
        final int MARGIN_B = 28;
        
        // Compact thresholds
        boolean compact1 = h < 760;      // move up a bit
        boolean compact2 = h < 640;      // 2-row layout
        
        int yBase = h - MARGIN_B - BTN_H;
        if (compact1) yBase = h - (MARGIN_B + BTN_H + 48);
        if (compact2) yBase = h - (MARGIN_B + BTN_H + 96);
        
        // Clamp Y not to go below 0
        yBase = Math.max(0, yBase);
        
        // Get button references
        HUDButton dealBtn = hudPanel.getHUDButton("deal");
        HUDButton hitBtn = hudPanel.getHUDButton("hit");
        HUDButton standBtn = hudPanel.getHUDButton("stand");
        HUDButton restartBtn = hudPanel.getHUDButton("restart");
        HUDButton setBalanceBtn = hudPanel.getHUDButton("setBalance");
        HUDButton undoBetBtn = hudPanel.getHUDButton("undoBet");
        HUDButton clearBetBtn = hudPanel.getHUDButton("clearBet");
        HUDButton payoutToggleBtn = hudPanel.getHUDButton("payoutToggle");
        
        if (!compact2) {
            // Single row, right-aligned
            int totalW = BTN_W * 4 + GAP * 3;
            int maxX = w - MARGIN_R - totalW;
            int x = Math.max(0, Math.min(maxX, w - totalW - 24)); // Safety clamp
            
            place(dealBtn, x + 0 * (BTN_W + GAP), yBase, BTN_W, BTN_H);
            place(hitBtn, x + 1 * (BTN_W + GAP), yBase, BTN_W, BTN_H);
            place(standBtn, x + 2 * (BTN_W + GAP), yBase, BTN_W, BTN_H);
            place(restartBtn, x + 3 * (BTN_W + GAP), yBase, BTN_W, BTN_H);
        } else {
            // Two rows: Deal/Hit on row1, Stand/Restart on row2
            int totalW = BTN_W * 2 + GAP;
            int x = Math.max(0, Math.min(w - MARGIN_R - totalW, w - totalW - 24)); // Safety clamp
            place(dealBtn, x + 0 * (BTN_W + GAP), yBase, BTN_W, BTN_H);
            place(hitBtn, x + 1 * (BTN_W + GAP), yBase, BTN_W, BTN_H);
            place(standBtn, x + 0 * (BTN_W + GAP), yBase + BTN_H + 8, BTN_W, BTN_H);
            place(restartBtn, x + 1 * (BTN_W + GAP), yBase + BTN_H + 8, BTN_W, BTN_H);
        }
        
        // Set Balance sits just left of the group; clamp too
        int sbW = 120;
        int sbH = 36;
        int totalW = BTN_W * 4 + GAP * 3;
        int sbX = Math.max(0, (compact2 ? w - MARGIN_R - (BTN_W * 2 + GAP) - sbW - GAP
                                        : w - MARGIN_R - totalW - GAP - sbW));
        int sbY = Math.max(0, yBase);
        place(setBalanceBtn, sbX, sbY, sbW, sbH);
        
        // Undo and Clear buttons below main buttons
        if (undoBetBtn != null && clearBetBtn != null) {
            int undoClearY = yBase + BTN_H + 10;
            int undoX = Math.max(0, sbX);
            int clearX = Math.max(0, sbX + sbW + GAP);
            place(undoBetBtn, undoX, undoClearY, sbW, sbH);
            place(clearBetBtn, clearX, undoClearY, sbW, sbH);
        }
        
        // Payout Toggle at top right
        if (payoutToggleBtn != null) {
            place(payoutToggleBtn, w - BTN_W - 20, 20, BTN_W, BTN_H);
        }
    }
    
    /**
     * Layout chip tray with proper constraints and gutter enforcement
     */
    private void layoutChipTray(int w, int h) {
        final int GUTTER = 24;
        
        // Compact thresholds (same as HUD)
        boolean compact1 = h < 760;      // move up a bit
        boolean compact2 = h < 640;      // 2-row layout
        
        // Tray area (left)
        int trayW = Math.min(420, Math.max(320, (int)(w * 0.34)));
        int trayH = 120;
        int trayX = 24;
        int trayY = Math.max(0, h - trayH - 32 - (compact1 ? 48 : 0) - (compact2 ? 48 : 0));
        
        // Create tray rectangle and set it
        Rectangle trayRect = new Rectangle(trayX, trayY, trayW, trayH);
        chipRackPanel.setTrayRect(trayRect);
        
        // Ensure HUD buttons don't overlap with tray
        int hudLeftX = w - 200; // Approximate HUD left edge
        if (hudLeftX < trayX + trayW + GUTTER) {
            // Tray is too close to HUD, adjust tray width
            trayW = Math.max(280, hudLeftX - trayX - GUTTER);
            trayRect = new Rectangle(trayX, trayY, trayW, trayH);
            chipRackPanel.setTrayRect(trayRect);
        }
    }
    
    /**
     * Helper method to place components with bounds clamping
     */
    private void place(JComponent c, int x, int y, int w, int h) {
        if (c != null) {
            c.setBounds(Math.max(0, x), Math.max(0, y), w, h);
        }
    }
    
    /**
     * Setup overlay callbacks for start screen and balance entry
     */
    private void setupOverlayCallbacks() {
        // Start overlay callbacks
        startOverlay.setOnStartCallback(() -> showBalanceEntry());
        startOverlay.setOnQuickStartCallback(() -> quickStartWithBalance100());
        startOverlay.setOnHelpCallback(() -> showHelpDialog());
        
        // Balance entry callbacks
        balanceEntryPanel.setOnContinueCallback(() -> handleBalanceEntryContinue());
        balanceEntryPanel.setOnCancelCallback(() -> hideBalanceEntry());
    }
    
    /**
     * Show balance entry panel
     */
    private void showBalanceEntry() {
        startOverlay.setVisible(false);
        balanceEntryPanel.setVisible(true);
        balanceEntryPanel.requestFocusInWindow();
        balanceEntryPanel.focusTextField();
    }
    
    /**
     * Hide balance entry panel and return to start overlay
     */
    private void hideBalanceEntry() {
        balanceEntryPanel.setVisible(false);
        startOverlay.setVisible(true);
        startOverlay.requestFocusInWindow();
    }
    
    /**
     * Handle balance entry continue
     */
    private void handleBalanceEntryContinue() {
        int amount = balanceEntryPanel.getBalanceAmount();
        engine.setBankroll(amount);
        engine.resetRoundKeepBankroll();
        refreshBalanceLabel();
        updateControls();
        
        // Hide overlay with fade out
        startOverlay.startFadeOut();
        balanceEntryPanel.setVisible(false);
        
        // Ensure HUD panel is visible when game starts
        if (hudPanel != null) {
            hudPanel.setVisible(true);
            layeredPane.moveToFront(hudPanel);
        }
    }
    
    /**
     * Quick start with balance 100
     */
    private void quickStartWithBalance100() {
        engine.setBankroll(100);
        engine.resetRoundKeepBankroll();
        refreshBalanceLabel();
        updateControls();
        
        // Hide overlay with fade out
        startOverlay.startFadeOut();
        
        // Ensure HUD panel is visible when game starts
        if (hudPanel != null) {
            hudPanel.setVisible(true);
            layeredPane.moveToFront(hudPanel);
        }
    }
    
    /**
     * Show help dialog
     */
    private void showHelpDialog() {
        String helpText = "BLACKJACK RULES:\n\n" +
                         "• Get as close to 21 as possible without going over\n" +
                         "• Face cards (J, Q, K) are worth 10\n" +
                         "• Aces can be 1 or 11 (automatically chosen)\n" +
                         "• Dealer must hit until 17 or higher\n" +
                         "• Blackjack (21 with 2 cards) pays 3:2\n" +
                         "• Win by having higher total than dealer\n" +
                         "• Lose if you go over 21 (bust)\n\n" +
                         "KEYBOARD SHORTCUTS:\n" +
                         "• B: Place Bet  • D: Deal  • H: Hit\n" +
                         "• S: Stand  • R: Restart  • Ctrl+L: Set Balance";
        
        JOptionPane.showMessageDialog(this, helpText, "Blackjack Help", 
                                    JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Disable all HUD buttons (used during start overlay)
     */
    private void disableHUDButtons() {
        setBalanceBtn.setEnabled(false);
        dealBtn.setEnabled(false);
        hitBtn.setEnabled(false);
        standBtn.setEnabled(false);
        restartBtn.setEnabled(false);
        undoBetBtn.setEnabled(false);
        clearBetBtn.setEnabled(false);
    }
    
    /**
     * Place a chip of the given denomination
     */
    private void placeChip(int denomination) {
        if (!betManager.canPlace(denomination, engine.getBankroll(), engine.getState())) {
            // Show error message
            if (engine.getState() != GameState.ROUND_START) {
                JOptionPane.showMessageDialog(this, "Can only place bets during ROUND_START", 
                                            "Invalid Action", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Insufficient funds for this bet", 
                                            "Invalid Bet", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
        
        // Find the rack button for spawn point
        Point spawnPoint = new Point(800, 600); // Default fallback
        if (chipRackPanel != null) {
            // Calculate spawn point from rack button
            spawnPoint = new Point(800, 600); // Will be improved with proper positioning
        }
        
        // Calculate target point with stacking offset
        Point betCenter = tablePanel.getBetCenter();
        if (betCenter == null) {
            betCenter = new Point(450, 350); // Default fallback
        }
        
        // Add stacking offset
        Point targetPoint = calculateStackOffset(betCenter, betManager.getChipCount());
        
        // Spawn chip sprite
        Color chipColor = BetManager.getColorForDenomination(denomination);
        ChipSprite chipSprite = animator.spawnChipSprite(spawnPoint, denomination, targetPoint, chipColor);
        
        // Add to bet manager
        betManager.addChip(denomination);
        betManager.addChipSprite(chipSprite);
        
        // Update UI
        refreshBalanceLabel();
        updateControls();
        tablePanel.setBetText("Bet: $" + betManager.getCurrentBet());
    }
    
    /**
     * Calculate stacking offset for chips
     */
    private Point calculateStackOffset(Point center, int chipIndex) {
        if (chipIndex == 0) {
            return center;
        }
        
        // Add small random offset for organic stacking
        double angle = chipIndex * 0.2; // Small angle increment
        int radius = 2 + (chipIndex % 3); // 2-4 pixel radius
        int offsetX = (int) (Math.cos(angle) * radius);
        int offsetY = (int) (Math.sin(angle) * radius);
        
        return new Point(center.x + offsetX, center.y + offsetY);
    }
    
    /**
     * Undo the last placed chip
     */
    private void undoLastChip() {
        if (betManager.getChipCount() == 0) {
            return;
        }
        
        // Get the last chip sprite
        var chips = betManager.getChips();
        if (!chips.isEmpty()) {
            ChipSprite lastChip = chips.get(chips.size() - 1);
            
            // Animate back to rack
            lastChip.animateBack();
            Point rackPoint = new Point(800, 600); // Will be improved
            lastChip.setTarget(rackPoint);
        }
        
        // Remove from bet manager
        betManager.removeLastChip();
        
        // Update UI
        refreshBalanceLabel();
        updateControls();
        tablePanel.setBetText("Bet: $" + betManager.getCurrentBet());
    }
    
    /**
     * Clear all placed chips
     */
    private void clearAllChips() {
        if (betManager.getChipCount() == 0) {
            return;
        }
        
        // Animate all chips back to rack
        var chips = betManager.getChips();
        for (int i = chips.size() - 1; i >= 0; i--) {
            ChipSprite chip = chips.get(i);
            chip.animateBack();
            Point rackPoint = new Point(800, 600); // Will be improved
            chip.setTarget(rackPoint);
        }
        
        // Clear bet manager
        betManager.clearAll();
        
        // Update UI
        refreshBalanceLabel();
        updateControls();
        tablePanel.setBetText("Bet: $0");
    }
    
    /**
     * Legacy control panel method removed - now using HUD buttons
     */
    
    /**
     * Spawn a bet chip for the given denomination
     */
    private void spawnBetChip(int denomination) {
        // Compute spawn point near bottom-right of table
        Point spawnPoint = new Point(tablePanel.getWidth() - 40, tablePanel.getHeight() - 40);
        
        // Get target bet center
        Point target = tablePanel.getBetCenter();
        
        // Choose color by denomination
        Color chipColor = chooseColorByDenom(denomination);
        
        // Spawn the chip
        animator.spawnChip(spawnPoint, target, chipColor);
        
        // Trigger repaint
        tablePanel.repaint();
        
        System.out.println("Spawned bet chip: $" + denomination);
    }
    
    /**
     * Choose classic chip color by denomination
     * @param denomination The chip denomination
     * @return Color for the chip
     */
    private Color chooseColorByDenom(int denomination) {
        switch (denomination) {
            case 5:   return Color.RED;
            case 10:  return Color.BLUE;
            case 25:  return Color.GREEN;
            case 100: return Color.BLACK;
            default:  return Color.WHITE;
        }
    }
    
    
    /**
     * Get the table panel (for testing purposes)
     * @return The TablePanel instance
     */
    public TablePanel getTablePanel() {
        return tablePanel;
    }
    
    /**
     * Update control button states based on game state
     */
    private void updateControls() {
        GameState s = engine.getState();
        
        // Default all disabled except Restart + Set Balance
        setBalanceBtn.setEnabled(true);
        dealBtn.setEnabled(false);
        hitBtn.setEnabled(false);
        standBtn.setEnabled(false);
        restartBtn.setEnabled(true);
        undoBetBtn.setEnabled(false);
        clearBetBtn.setEnabled(false);

        switch (s) {
            case ROUND_START -> {
                // Enable chip rack for betting
                chipRackPanel.setEnabledRack(engine.getBankroll() > 0);
                chipRackPanel.setVisible(true);
                // Enable Deal only if there's a bet placed
                dealBtn.setEnabled(betManager.getCurrentBet() > 0);
                // Enable Undo/Clear only if there are chips placed
                boolean hasChips = betManager.getChipCount() > 0;
                undoBetBtn.setEnabled(hasChips);
                clearBetBtn.setEnabled(hasChips);
            }
            case BET_PLACED -> {
                dealBtn.setEnabled(true);
                // Disable chip rack and bet controls after bet is placed
                chipRackPanel.setEnabledRack(false);
            }
            case PLAYER_TURN -> {
                hitBtn.setEnabled(true);
                standBtn.setEnabled(true);
                // Keep chip rack disabled during play
                chipRackPanel.setEnabledRack(false);
            }
            case DEALER_TURN -> {
                // All disabled during dealer turn
                chipRackPanel.setEnabledRack(false);
            }
            case ROUND_END -> {
                // Only Restart enabled
                chipRackPanel.setEnabledRack(false);
            }
        }
    }
    
    /**
     * Refresh the balance label with current bankroll and bet
     */
    private void refreshBalanceLabel() {
        int currentBet = betManager.getCurrentBet();
        balanceLabel.setText("Balance: $" + engine.getBankroll() + 
                             (currentBet > 0 ? ("  | Bet: $" + currentBet) : ""));
    }
    
    /**
     * Update table display based on current game state
     */
    private void updateTableDisplay() {
        boolean hideDealerHole = (engine.getState() == GameState.PLAYER_TURN);
        tablePanel.setHands(engine.getPlayer().getCards(), engine.getDealer().getCards(), hideDealerHole);
        
        // Update hand labels
        dealerHandLabel.setText("Dealer: " + engine.getDealer().toString() + " (Value: " + engine.getDealer().value() + ")");
        playerHandLabel.setText("Player: " + engine.getPlayer().toString() + " (Value: " + engine.getPlayer().value() + ")");
        
        // Ensure repaint happens
        tablePanel.repaint();
    }
    
    /**
     * Show outcome dialog and lock controls
     */
    private void showOutcomeDialogAndLock() {
        JOptionPane.showMessageDialog(this, engine.getLastOutcome(), "Round Result", JOptionPane.INFORMATION_MESSAGE);
        updateControls(); // state is ROUND_END now
    }
    
    /**
     * Refresh overlay totals based on current game state
     */
    private void refreshTotalsForState() {
        var s = engine.getState();
        var p = engine.getPlayer();
        var d = engine.getDealer();

        String playerTxt = "Total: " + p.value() + (p.isSoft() ? " (soft)" : "");
        String dealerTxt;

        if (s == GameState.PLAYER_TURN) {
            // Only show dealer's up-card value
            int up = 0;
            if (!d.getCards().isEmpty()) {
               bj.model.Card upCard = d.getCards().get(0);
               up = upCard.getRank().getValue();
               // If up-card is an Ace, showing value "11" is fine for info; it's only a hint.
            }
            dealerTxt = "Showing: " + up;
        } else {
            // For ROUND_END and other states, show full dealer total (hole card revealed)
            dealerTxt = "Total: " + d.value() + (d.isSoft() ? " (soft)" : "");
        }

        // If TablePanel:
        tablePanel.setTotals(playerTxt, dealerTxt);
        // If labels:
        // dealerTotalLabel.setText(dealerTxt);
        // playerTotalLabel.setText(playerTxt);
    }
    
    /**
     * Set Balance button action
     */
    private void setBalance() {
        String input = JOptionPane.showInputDialog(this, "Enter starting balance:", "Set Balance", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int balance = Integer.parseInt(input.trim());
                if (balance > 0) {
                    engine.setBankroll(balance);
                    engine.resetRoundKeepBankroll(); // Ensure state = ROUND_START
                    refreshBalanceLabel();
                    updateControls();
                } else {
                    JOptionPane.showMessageDialog(this, "Balance must be positive!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Deal button action - updated to use BlackjackEngine
     */
    private void deal() {
        // Check if there's a bet placed using chips
        if (betManager.getCurrentBet() <= 0) {
            JOptionPane.showMessageDialog(this, "Please place chips to bet before dealing!", 
                                        "No Bet Placed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Place the bet using the chip amount
            engine.placeBet(betManager.getCurrentBet());
            engine.startNewRound();
            
            // Update hands on the table with proper hole card visibility
            tablePanel.setHands(engine.getPlayer().getCards(),
                               engine.getDealer().getCards(),
                               /*hideDealerHole=*/engine.dealerHoleShouldBeHidden());
            
            // Refresh balance label
            refreshBalanceLabel();
            
            // Check if round ended due to natural blackjack
            if (engine.isRoundOver()) {
                // Natural happened - reveal dealer hole for ROUND_END view
                tablePanel.setHands(engine.getPlayer().getCards(),
                                   engine.getDealer().getCards(),
                                   /*hideDealerHole=*/false);
                refreshTotalsForState(); // will now show real dealer total
                showOutcomeDialogAndLock(); // same helper from step 4
            } else {
                // No natural → proceed with normal PLAYER_TURN
                refreshTotalsForState();
                updateControls();
            }
            
            // Print debug information
            System.out.println("Deal! - Engine Mode");
            System.out.println("Dealer hand: " + engine.getDealer().toString() + " (Value: " + engine.getDealer().value() + ")");
            System.out.println("Player hand: " + engine.getPlayer().toString() + " (Value: " + engine.getPlayer().value() + ")");
            System.out.println("Game state: " + engine.getState());
            
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Deal", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Hit button action
     */
    private void hit() {
        try {
            engine.playerHit();
            
            // Update table visuals (hands; hideDealerHole depends on state)
            updateTableDisplay();
            
            // Refresh overlay totals
            refreshTotalsForState();
            
            // Refresh balance label
            refreshBalanceLabel();
            
            // If round is over, show outcome dialog and lock controls
            if (engine.isRoundOver()) {
                showOutcomeDialogAndLock();
            }
            
            System.out.println("Hit! - State: " + engine.getState());
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Hit", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Stand button action
     */
    private void stand() {
        try {
            engine.playerStand();
            
            // Update table visuals (now dealer hole card must be visible)
            updateTableDisplay();
            
            // Refresh overlay totals
            refreshTotalsForState();
            
            // Refresh balance label
            refreshBalanceLabel();
            
            // Show outcome dialog and lock controls
            showOutcomeDialogAndLock();
            
            System.out.println("Stand! - State: " + engine.getState());
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Stand", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Restart button action
     */
    private void restart() {
        engine.resetRoundKeepBankroll();
        
        // Clear all chips and bet
        betManager.clearAll();
        animator.clearChipSprites();
        tablePanel.setBetText("Bet: $0");
        
        // Clear table visuals (empty hands, hideDealerHole=true)
        tablePanel.setHands(java.util.Collections.emptyList(), java.util.Collections.emptyList(), true);
        
        // Clear hand labels
        dealerHandLabel.setText("Dealer: ");
        playerHandLabel.setText("Player: ");
        
        // Clear overlay totals
        refreshTotalsForState();
        
        refreshBalanceLabel();
        updateControls();
        
        System.out.println("Game restarted - State: " + engine.getState());
    }
    
    /**
     * Main method to run the application
     */
    public static void main(String[] args) {
        // Create and show the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new BlackjackApp();
        });
    }
    
    /**
     * FullscreenManager handles all fullscreen operations
     */
    private static class FullscreenManager {
        private final JFrame frame;
        private boolean isFullscreen = false;
        private Rectangle windowedBounds = new Rectangle(100, 100, 1100, 700);
        
        public FullscreenManager(JFrame frame) {
            this.frame = frame;
            
            // Set macOS fullscreen property
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                frame.getRootPane().putClientProperty("apple.awt.fullscreenable", true);
            }
        }
        
        /**
         * Get current graphics device
         */
        private GraphicsDevice currentDevice() {
            return frame.getGraphicsConfiguration().getDevice();
        }
        
        /**
         * Get screen bounds for current device
         */
        private Rectangle screenBounds() {
            return currentDevice().getDefaultConfiguration().getBounds();
        }
        
        /**
         * Enter fullscreen mode
         */
        public void enterFullscreen() {
            GraphicsDevice device = currentDevice();
            
            try {
                if (device.isFullScreenSupported()) {
                    // Try exclusive fullscreen
                    frame.dispose();
                    frame.setUndecorated(true);
                    frame.setResizable(false);
                    device.setFullScreenWindow(frame);
                    isFullscreen = true;
                } else {
                    // Fallback to borderless window
                    enterBorderless();
                }
            } catch (Exception e) {
                // Fallback to borderless or maximized
                try {
                    enterBorderless();
                } catch (Exception e2) {
                    // Last resort: maximized
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    isFullscreen = false;
                }
            }
            
            // Relayout after entering fullscreen
            SwingUtilities.invokeLater(() -> {
                Dimension sz = frame.getContentPane().getSize();
                ((BlackjackApp) frame).relayoutAll();
                // Ensure HUD panel is visible and on top
                ((BlackjackApp) frame).hudPanel.setVisible(true);
                ((BlackjackApp) frame).chipRackPanel.setVisible(true);
                ((BlackjackApp) frame).hudPanel.revalidate();
                ((BlackjackApp) frame).hudPanel.repaint();
                ((BlackjackApp) frame).layeredPane.revalidate();
                ((BlackjackApp) frame).layeredPane.repaint();
                // Ensure overlay stays visible if it was visible
                if (frame.getRootPane().getGlassPane().isVisible()) {
                    frame.getRootPane().getGlassPane().requestFocusInWindow();
                }
            });
        }
        
        /**
         * Enter borderless fullscreen mode
         */
        private void enterBorderless() {
            Rectangle screenBounds = screenBounds();
            frame.dispose();
            frame.setUndecorated(true);
            frame.setResizable(false);
            frame.setBounds(screenBounds);
            frame.setVisible(true);
            isFullscreen = true;
        }
        
        /**
         * Exit fullscreen mode
         */
        public void exitFullscreen() {
            GraphicsDevice device = currentDevice();
            
            // Clear exclusive fullscreen
            device.setFullScreenWindow(null);
            
            // Restore windowed mode
            frame.dispose();
            frame.setUndecorated(false);
            frame.setResizable(true);
            frame.setBounds(windowedBounds);
            frame.setVisible(true);
            frame.setExtendedState(JFrame.NORMAL);
            isFullscreen = false;
            
            // Relayout after exiting fullscreen
            SwingUtilities.invokeLater(() -> {
                Dimension sz = frame.getContentPane().getSize();
                ((BlackjackApp) frame).relayoutAll();
                // Ensure HUD panel is visible and on top
                ((BlackjackApp) frame).hudPanel.setVisible(true);
                ((BlackjackApp) frame).chipRackPanel.setVisible(true);
                ((BlackjackApp) frame).hudPanel.revalidate();
                ((BlackjackApp) frame).hudPanel.repaint();
                ((BlackjackApp) frame).layeredPane.revalidate();
                ((BlackjackApp) frame).layeredPane.repaint();
                // Ensure overlay stays visible if it was visible
                if (frame.getRootPane().getGlassPane().isVisible()) {
                    frame.getRootPane().getGlassPane().requestFocusInWindow();
                }
            });
        }
        
        /**
         * Toggle fullscreen mode
         */
        public void toggleFullscreen() {
            if (!isFullscreen) {
                // Save current bounds before entering fullscreen
                windowedBounds = frame.getBounds();
                enterFullscreen();
            } else {
                exitFullscreen();
            }
        }
        
        /**
         * Check if currently in fullscreen mode
         */
        public boolean isFullscreen() {
            return isFullscreen;
        }
    }
}
