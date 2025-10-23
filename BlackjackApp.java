package bj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import bj.model.Deck;
import bj.model.Hand;
import bj.logic.BlackjackEngine;
import bj.logic.GameState;
import bj.ui.bet.BetManager;
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
    private BottomDockPanel bottomDockPanel;
    
    // Start overlay components
    private StartOverlayPanel startOverlay;
    private BalanceEntryPanel balanceEntryPanel;
    
    // Fullscreen support
    private GameLayeredPane layeredPane;
    private FullscreenManager fullscreenManager;
    
    // Chip betting components
    private BetManager betManager;
    private ChipTrayWidget chipTrayWidget;
    
    // Result card
    private ResultCardPanel resultCard;
    
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
        // Create root container with BorderLayout
        JPanel root = new JPanel(new BorderLayout());
        
        // Create game layered pane for table and animations
        layeredPane = new GameLayeredPane();
        
        // Add table panel to DEFAULT_LAYER
        layeredPane.add(tablePanel, JLayeredPane.DEFAULT_LAYER);
        
        // Create start overlay
        startOverlay = new StartOverlayPanel(this);
        
        // Create balance entry panel
        balanceEntryPanel = new BalanceEntryPanel();
        layeredPane.add(balanceEntryPanel, JLayeredPane.DRAG_LAYER);
        balanceEntryPanel.setVisible(false);
        
        // Create chip tray widget
        chipTrayWidget = new ChipTrayWidget();
        chipTrayWidget.setEnabled(true);
        
        // Create result card
        resultCard = new ResultCardPanel();
        layeredPane.add(resultCard, JLayeredPane.DRAG_LAYER);
        
        // Create HUD buttons
        createHUDButtons();
        
        // Create bottom dock panel
        bottomDockPanel = new BottomDockPanel();
        
        // Setup overlay callbacks
        setupOverlayCallbacks();
        
        // Setup chip tray callback
        chipTrayWidget.setOnChipClick(this::placeChip);
        
        // Setup result card callbacks
        resultCard.setNewRoundAction(e -> newRound());
        resultCard.setLeaveTableAction(e -> leaveTable());
        
        // Add components to root container
        root.add(balanceLabel, BorderLayout.NORTH);
        root.add(layeredPane, BorderLayout.CENTER);
        root.add(bottomDockPanel, BorderLayout.SOUTH);
        
        // Set the root as content pane
        setContentPane(root);
        
        // Initialize fullscreen manager
        fullscreenManager = new FullscreenManager(this);
        
        // Install start overlay as glass pane for guaranteed full coverage
        getRootPane().setGlassPane(startOverlay);
        startOverlay.setVisible(false);
        startOverlay.setOpaque(false);
        startOverlay.setEnabled(false); // Disable during gameplay
        
        // Configure frame
        setPreferredSize(new Dimension(900, 700));
        pack();
        setLocationRelativeTo(null);
        
        // Show the frame
        setVisible(true);
        
        // Setup key bindings for fullscreen
        setupKeyBindings();
        
        // Wire dock components after frame is visible
        SwingUtilities.invokeLater(() -> {
            wireDockComponents();
            animator.start();
            
            // Enter fullscreen mode
            fullscreenManager.enterFullscreen();
            
            // Show start overlay and disable HUD buttons initially
            startOverlay.setVisible(true);
            startOverlay.setEnabled(true);
            startOverlay.setFocusable(true);
            startOverlay.requestFocusInWindow();
            disableHUDButtons();
            
            // Ensure updateControls is called to set proper button states
            updateControls();
        });
        
        // Add component listener for resize handling
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                // Check if we need two-row layout for compact screens
                Dimension size = getContentPane().getSize();
                if (size.height < 700) {
                    bottomDockPanel.enableTwoRowLayout();
                } else {
                    bottomDockPanel.enableSingleRowLayout();
                }
                
                // Ensure proper validation and state update
                SwingUtilities.invokeLater(() -> {
                    validate();
                    repaint();
                    updateControls(); // Ensure button states are correct after resize
                    
                    // Update result card position after resize
                    resultCard.updatePosition();
                });
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
        
        // Ensure buttons are focusable and enabled by default
        setBalanceBtn.setFocusable(true);
        dealBtn.setFocusable(true);
        hitBtn.setFocusable(true);
        standBtn.setFocusable(true);
        restartBtn.setFocusable(true);
        undoBetBtn.setFocusable(true);
        clearBetBtn.setFocusable(true);
        
        // Add action listeners (reuse existing methods)
        setBalanceBtn.addActionListener(e -> setBalance());
        dealBtn.addActionListener(e -> deal());
        hitBtn.addActionListener(e -> hit());
        standBtn.addActionListener(e -> stand());
        restartBtn.addActionListener(e -> restart());
        undoBetBtn.addActionListener(e -> undoLastChip());
        clearBetBtn.addActionListener(e -> clearAllChips());
    }
    
    /**
     * Wire dock components to their respective panels
     */
    private void wireDockComponents() {
        // Add chip tray widget to left panel
        bottomDockPanel.getLeftPanel().add(chipTrayWidget);
        
        // Add main buttons to right panel in order: Set Balance | Deal | Hit | Stand | Restart
        bottomDockPanel.getRightPanel().add(setBalanceBtn);
        bottomDockPanel.getRightPanel().add(dealBtn);
        bottomDockPanel.getRightPanel().add(hitBtn);
        bottomDockPanel.getRightPanel().add(standBtn);
        bottomDockPanel.getRightPanel().add(restartBtn);
        
        // Add Undo and Clear buttons (will be placed in second row if needed)
        bottomDockPanel.addUndoClear(undoBetBtn, clearBetBtn);
        
        // Check if we need two-row layout initially
        Dimension size = getContentPane().getSize();
        if (size.height < 700) {
            bottomDockPanel.enableTwoRowLayout();
        }
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
        
        // F9 for overlay diagnostics
        inputMap.put(KeyStroke.getKeyStroke("F9"), "showOverlayDiagnostics");
        actionMap.put("showOverlayDiagnostics", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOverlayDiagnostics();
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
        
        // Hide start overlay
        startOverlay.startFadeOut();
        balanceEntryPanel.setVisible(false);
        
        // Ensure glass pane is hidden and disabled during gameplay
        if (getRootPane().getGlassPane() != null) {
            getRootPane().getGlassPane().setVisible(false);
            getRootPane().getGlassPane().setEnabled(false);
        }
        
        // Dock panel is always visible (managed by BorderLayout)
    }
    
    /**
     * Quick start with balance 100
     */
    private void quickStartWithBalance100() {
        engine.setBankroll(100);
        engine.resetRoundKeepBankroll();
        refreshBalanceLabel();
        updateControls();
        
        // Hide start overlay
        startOverlay.startFadeOut();
        
        // Ensure glass pane is hidden and disabled during gameplay
        if (getRootPane().getGlassPane() != null) {
            getRootPane().getGlassPane().setVisible(false);
            getRootPane().getGlassPane().setEnabled(false);
        }
        
        // Dock panel is always visible (managed by BorderLayout)
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
        
        // Find the chip tray for spawn point
        Point spawnPoint = new Point(800, 600); // Default fallback
        if (chipTrayWidget != null) {
            // Calculate spawn point from chip tray widget
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
                // Enable chip tray for betting
                chipTrayWidget.setEnabled(engine.getBankroll() > 0);
                // Enable Deal only if there's a bet placed
                dealBtn.setEnabled(betManager.getCurrentBet() > 0);
                // Enable Undo/Clear only if there are chips placed
                boolean hasChips = betManager.getChipCount() > 0;
                undoBetBtn.setEnabled(hasChips);
                clearBetBtn.setEnabled(hasChips);
            }
            case BET_PLACED -> {
                dealBtn.setEnabled(true);
                // Disable chip tray and bet controls after bet is placed
                chipTrayWidget.setEnabled(false);
            }
            case PLAYER_TURN -> {
                hitBtn.setEnabled(true);
                standBtn.setEnabled(true);
                // Keep chip tray disabled during play
                chipTrayWidget.setEnabled(false);
            }
            case DEALER_TURN -> {
                // All disabled during dealer turn
                chipTrayWidget.setEnabled(false);
            }
            case ROUND_END -> {
                // Only Restart enabled
                chipTrayWidget.setEnabled(false);
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
        dealerHandLabel.setText("Dealer: " + engine.getDealer().getLocalizedString() + " (Value: " + engine.getDealer().value() + ")");
        playerHandLabel.setText("Player: " + engine.getPlayer().getLocalizedString() + " (Value: " + engine.getPlayer().value() + ")");
        
        // Ensure repaint happens
        tablePanel.repaint();
    }
    
    /**
     * Show result card and lock controls
     */
    private void showResultAndLock() {
        // Ensure this runs on EDT
        SwingUtilities.invokeLater(() -> {
            // Reveal dealer hole card
            tablePanel.setHands(engine.getPlayer().getCards(),
                               engine.getDealer().getCards(),
                               /*hideDealerHole=*/false);
            
            // Show result card with proper theme
            String outcome = engine.getLastOutcome();
            int netChange = calculateNetChange();
            int currentBalance = engine.getBankroll();
            ResultCardModel.Theme theme = determineTheme(outcome, netChange);
            
            if (!outcome.isEmpty()) {
                resultCard.showResultCard(outcome, netChange, currentBalance, theme);
            }
            
            // Lock controls - only Restart and Set Balance enabled
            updateControls();
            
            // Force repaint
            tablePanel.repaint();
        });
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
                showResultAndLock(); // Show result banner and lock controls
            } else {
                // No natural → proceed with normal PLAYER_TURN
                refreshTotalsForState();
                updateControls();
            }
            
            // Print debug information
            System.out.println("Deal! - Engine Mode");
            System.out.println("Dealer hand: " + engine.getDealer().getLocalizedString() + " (Value: " + engine.getDealer().value() + ")");
            System.out.println("Player hand: " + engine.getPlayer().getLocalizedString() + " (Value: " + engine.getPlayer().value() + ")");
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
            
            // If round is over, show result banner and lock controls
            if (engine.isRoundOver()) {
                showResultAndLock();
            } else {
                // Update controls for continued play
                updateControls();
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
            
            // Show result banner and lock controls
            showResultAndLock();
            
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
        // Ensure this runs on EDT
        SwingUtilities.invokeLater(() -> {
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
            
            // Hide result card
            resultCard.hideResultCard();
            
            // Clear overlay totals
            refreshTotalsForState();
            
            refreshBalanceLabel();
            updateControls();
            
            // Force repaint
            tablePanel.repaint();
            
            System.out.println("Game restarted - State: " + engine.getState());
        });
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
    /**
     * Calculate net change for result card
     */
    private int calculateNetChange() {
        // This is a simplified calculation - in a real implementation,
        // you'd track the balance before and after the round
        return engine.getCurrentBet(); // For now, assume the bet amount as net change
    }
    
    /**
     * Determine theme based on outcome and net change
     */
    private ResultCardModel.Theme determineTheme(String outcome, int netChange) {
        if (outcome.toLowerCase().contains("blackjack")) {
            return ResultCardModel.Theme.BJ;
        } else if (netChange > 0) {
            return ResultCardModel.Theme.WIN;
        } else if (netChange < 0) {
            return ResultCardModel.Theme.LOSE;
        } else {
            return ResultCardModel.Theme.PUSH;
        }
    }
    
    /**
     * New round action from result card
     */
    private void newRound() {
        restart();
    }
    
    /**
     * Leave table action from result card
     */
    private void leaveTable() {
        // Hide result card
        resultCard.hideResultCard();
        
        // Show IDLE state (simple message)
        JOptionPane.showMessageDialog(this, 
            "You have left the table.\nClick 'Sit Back Down' to return to the game.",
            "Left Table", JOptionPane.INFORMATION_MESSAGE);
        
        // Return to ROUND_START state
        engine.resetRoundKeepBankroll();
        refreshBalanceLabel();
        updateControls();
    }
    
    /**
     * Show overlay diagnostics (F9 key)
     */
    private void showOverlayDiagnostics() {
        String diagnostics = String.format(
            "Result Card Diagnostics:\n" +
            "Card Visible: %s\n" +
            "Game State: %s\n" +
            "Bankroll: $%d\n" +
            "Current Bet: $%d\n" +
            "Last Outcome: %s",
            resultCard.isCardVisible(),
            engine.getState(),
            engine.getBankroll(),
            engine.getCurrentBet(),
            engine.getLastOutcome()
        );
        
        JOptionPane.showMessageDialog(this, diagnostics, "Result Card Diagnostics", JOptionPane.INFORMATION_MESSAGE);
    }
    
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
            
            // Validate and repaint after entering fullscreen
            SwingUtilities.invokeLater(() -> {
                frame.validate();
                frame.repaint();
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
            
            // Validate and repaint after exiting fullscreen
            SwingUtilities.invokeLater(() -> {
                frame.validate();
                frame.repaint();
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
