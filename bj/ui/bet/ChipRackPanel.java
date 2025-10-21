package bj.ui.bet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;
import bj.ui.TablePanel;

/**
 * Chip rack panel that displays denomination buttons and handles chip placement.
 * Positioned as a transparent overlay on the table.
 */
public class ChipRackPanel extends JPanel implements KeyListener {
    
    private static final Color RACK_COLOR = new Color(30, 30, 30, 180);
    private static final Color RACK_BORDER_COLOR = new Color(255, 255, 255, 60);
    
    private final RackChipButton[] chipButtons;
    private final TablePanel tablePanel;
    private java.util.function.Consumer<Integer> onChipPlaceCallback;
    
    // Drag and drop support
    private boolean dragging = false;
    private Point dragStartPoint = null;
    private int draggedDenomination = 0;
    private Point dragCurrentPoint = null;
    
    public ChipRackPanel(TablePanel tablePanel) {
        this.tablePanel = tablePanel;
        
        // Create chip buttons for denominations 5, 10, 25, 100
        chipButtons = new RackChipButton[4];
        chipButtons[0] = new RackChipButton(5);
        chipButtons[1] = new RackChipButton(10);
        chipButtons[2] = new RackChipButton(25);
        chipButtons[3] = new RackChipButton(100);
        
        setupPanel();
        setupKeyboardShortcuts();
    }
    
    private void setupPanel() {
        setOpaque(false);
        setFocusable(true);
        setLayout(null);
        
        // Add chip buttons
        for (RackChipButton button : chipButtons) {
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    placeChip(button.getDenomination());
                }
            });
            add(button);
        }
        
        // Add mouse listeners for drag and drop
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                handleMousePress(e);
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                handleMouseRelease(e);
            }
        });
        
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                handleMouseDrag(e);
            }
        });
    }
    
    private void setupKeyboardShortcuts() {
        addKeyListener(this);
        
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        // Map keys 1-4 to denominations 5, 10, 25, 100
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "chip5");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "chip10");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), "chip25");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0), "chip100");
        
        actionMap.put("chip5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeChip(5);
            }
        });
        
        actionMap.put("chip10", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeChip(10);
            }
        });
        
        actionMap.put("chip25", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeChip(25);
            }
        });
        
        actionMap.put("chip100", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeChip(100);
            }
        });
    }
    
    /**
     * Layout chip buttons in a rack formation
     */
    public void layoutButtons() {
        if (tablePanel == null) return;
        
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        // Position rack in bottom-right area, avoiding bet circle
        int rackWidth = 280;
        int rackHeight = 80;
        int rackX = panelWidth - rackWidth - 20;
        int rackY = panelHeight - rackHeight - 20;
        
        // Layout chip buttons in a row
        int buttonWidth = 60;
        int buttonHeight = 60;
        int spacing = 10;
        int startX = rackX + 10;
        int startY = rackY + 10;
        
        for (int i = 0; i < chipButtons.length; i++) {
            int x = startX + i * (buttonWidth + spacing);
            chipButtons[i].setBounds(x, startY, buttonWidth, buttonHeight);
        }
    }
    
    /**
     * Place a chip of the given denomination
     */
    private void placeChip(int denomination) {
        if (onChipPlaceCallback != null) {
            onChipPlaceCallback.accept(denomination);
        }
    }
    
    /**
     * Handle mouse press for drag initiation
     */
    private void handleMousePress(java.awt.event.MouseEvent e) {
        for (RackChipButton button : chipButtons) {
            if (button.getBounds().contains(e.getPoint())) {
                dragging = true;
                dragStartPoint = e.getPoint();
                draggedDenomination = button.getDenomination();
                dragCurrentPoint = e.getPoint();
                button.doClick(); // Trigger button action for immediate placement
                break;
            }
        }
    }
    
    /**
     * Handle mouse drag for drag visualization
     */
    private void handleMouseDrag(java.awt.event.MouseEvent e) {
        if (dragging) {
            dragCurrentPoint = e.getPoint();
            repaint();
        }
    }
    
    /**
     * Handle mouse release for drag completion
     */
    private void handleMouseRelease(java.awt.event.MouseEvent e) {
        if (dragging) {
            // Check if released over bet circle
            Point betCenter = tablePanel.getBetCenter();
            if (betCenter != null) {
                double distance = e.getPoint().distance(betCenter);
                if (distance < 50) { // Within bet circle area
                    placeChip(draggedDenomination);
                }
            }
            
            dragging = false;
            dragStartPoint = null;
            draggedDenomination = 0;
            dragCurrentPoint = null;
            repaint();
        }
    }
    
    /**
     * Set callback for chip placement
     */
    public void setOnChipPlaceCallback(java.util.function.Consumer<Integer> callback) {
        this.onChipPlaceCallback = callback;
    }
    
    /**
     * Enable or disable all chip buttons
     */
    public void setChipButtonsEnabled(boolean enabled) {
        for (RackChipButton button : chipButtons) {
            button.setEnabled(enabled);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        // Calculate rack bounds
        int rackWidth = 280;
        int rackHeight = 80;
        int rackX = panelWidth - rackWidth - 20;
        int rackY = panelHeight - rackHeight - 20;
        
        // Draw rack background
        g2d.setColor(RACK_COLOR);
        RoundRectangle2D rack = new RoundRectangle2D.Float(rackX, rackY, rackWidth, rackHeight, 15, 15);
        g2d.fill(rack);
        
        // Draw rack border
        g2d.setColor(RACK_BORDER_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(rack);
        
        // Draw rack label
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        String label = "CHIPS";
        int labelX = rackX + 10;
        int labelY = rackY - 5;
        g2d.drawString(label, labelX, labelY);
        
        // Draw drag preview if dragging
        if (dragging && dragCurrentPoint != null) {
            drawDragPreview(g2d, dragCurrentPoint, draggedDenomination);
        }
        
        g2d.dispose();
    }
    
    /**
     * Draw a preview of the chip being dragged
     */
    private void drawDragPreview(Graphics2D g2d, Point position, int denomination) {
        Color chipColor = BetManager.getColorForDenomination(denomination);
        int radius = BetManager.getRadiusForDenomination(denomination);
        
        // Draw semi-transparent chip preview
        g2d.setComposite(AlphaComposite.SrcOver.derive(0.7f));
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 80));
        Ellipse2D shadow = new Ellipse2D.Float(position.x - radius + 2, position.y - radius + 3, 
                                             radius * 2, radius * 2);
        g2d.fill(shadow);
        
        // Draw chip
        g2d.setColor(chipColor);
        Ellipse2D chip = new Ellipse2D.Float(position.x - radius, position.y - radius, 
                                           radius * 2, radius * 2);
        g2d.fill(chip);
        
        // Draw white ring
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(chip);
        
        // Draw denomination text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, Math.max(10, radius / 2)));
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(denomination);
        int textX = position.x - fm.stringWidth(text) / 2;
        int textY = position.y + fm.getAscent() / 2 - 2;
        g2d.drawString(text, textX, textY);
        
        g2d.setComposite(AlphaComposite.SrcOver); // Reset alpha
    }
    
    // KeyListener implementation
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyPressed(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
}
