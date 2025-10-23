package bj.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A modern bottom dock panel that provides a fixed-height control area at the bottom
 * of the screen. Uses layout managers to ensure controls are always visible
 * and properly positioned with a subtle dark belt gradient.
 */
public class BottomDockPanel extends JPanel {
    
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel rightWrap; // For two-row layout when height < 700
    private JPanel rightRow1;
    private JPanel rightRow2;
    
    // Colors for the subtle dark belt gradient
    private static final Color BELT_TOP = new Color(0, 0, 0, 80);
    private static final Color BELT_BOTTOM = new Color(0, 0, 0, 120);
    private static final Color HIGHLIGHT = new Color(255, 255, 255, 30);
    
    public BottomDockPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(16, 0));
        
        // Set size constraints for fixed height dock
        setPreferredSize(new Dimension(10, 130));
        setMinimumSize(new Dimension(10, 120));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        
        // Temporary diagnostic border
        setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        
        // Create left panel for chip tray with limited width
        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(420, 120));
        leftPanel.setMaximumSize(new Dimension(480, 140)); // Limit maximum width
        add(leftPanel, BorderLayout.WEST);
        
        // Create right panel for gameplay buttons
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(600, 120)); // Guarantee space for buttons
        add(rightPanel, BorderLayout.EAST);
    }
    
    /**
     * Get the left panel for chip tray components
     * @return The left panel
     */
    public JPanel getLeftPanel() {
        return leftPanel;
    }
    
    /**
     * Get the right panel for gameplay buttons
     * @return The right panel
     */
    public JPanel getRightPanel() {
        return rightPanel;
    }
    
    /**
     * Switch to two-row layout for compact screens (height < 700)
     */
    public void enableTwoRowLayout() {
        if (rightWrap != null) {
            return; // Already in two-row mode
        }
        
        // Remove the single right panel
        remove(rightPanel);
        
        // Create wrapper for two rows
        rightWrap = new JPanel();
        rightWrap.setOpaque(false);
        rightWrap.setLayout(new BoxLayout(rightWrap, BoxLayout.Y_AXIS));
        
        // Create first row for main buttons
        rightRow1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 6));
        rightRow1.setOpaque(false);
        
        // Create second row for Undo/Clear buttons
        rightRow2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 6));
        rightRow2.setOpaque(false);
        
        rightWrap.add(rightRow1);
        rightWrap.add(rightRow2);
        add(rightWrap, BorderLayout.EAST);
        
        revalidate();
        repaint();
    }
    
    /**
     * Switch back to single-row layout
     */
    public void enableSingleRowLayout() {
        if (rightWrap == null) {
            return; // Already in single-row mode
        }
        
        // Remove the two-row wrapper
        remove(rightWrap);
        
        // Restore single right panel
        add(rightPanel, BorderLayout.EAST);
        
        rightWrap = null;
        rightRow1 = null;
        rightRow2 = null;
        
        revalidate();
        repaint();
    }
    
    /**
     * Add Undo and Clear buttons to the second row
     * @param undoBtn The undo button
     * @param clearBtn The clear button
     */
    public void addUndoClear(JComponent undoBtn, JComponent clearBtn) {
        if (rightRow2 != null) {
            rightRow2.add(undoBtn);
            rightRow2.add(clearBtn);
        } else {
            // Fallback to single row if not in two-row mode
            rightPanel.add(undoBtn);
            rightPanel.add(clearBtn);
        }
    }
    
    /**
     * Get the first row panel (for main buttons in two-row mode)
     * @return The first row panel, or null if in single-row mode
     */
    public JPanel getRightRow1() {
        return rightRow1;
    }
    
    /**
     * Get the second row panel (for Undo/Clear buttons in two-row mode)
     * @return The second row panel, or null if in single-row mode
     */
    public JPanel getRightRow2() {
        return rightRow2;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // No background painting to avoid event interception
        // Let child components handle their own backgrounds
    }
}
