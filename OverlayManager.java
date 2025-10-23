package bj.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Centralized overlay manager responsible for start overlay and result banner.
 * Ensures only one overlay is visible at a time and provides non-blocking lifecycle.
 */
public class OverlayManager {
    
    private final JFrame frame;
    private final StartOverlayPanel startOverlay;
    private final ResultBanner resultBanner;
    
    // State flags
    private boolean isStartVisible = false;
    private boolean isResultVisible = false;
    private boolean isAnimating = false;
    
    // Auto-hide timer for result banner
    private Timer autoHideTimer;
    
    public OverlayManager(JFrame frame, StartOverlayPanel startOverlay, ResultBanner resultBanner) {
        this.frame = frame;
        this.startOverlay = startOverlay;
        this.resultBanner = resultBanner;
        
        // Setup auto-hide timer
        autoHideTimer = new Timer(2000, e -> hideResult());
    }
    
    /**
     * Show start overlay (app launch/balance set)
     */
    public void showStart() {
        if (isAnimating) return; // Prevent double-shows
        
        isAnimating = true;
        isStartVisible = true;
        isResultVisible = false;
        
        // Hide result banner first
        resultBanner.dismiss();
        
        // Show start overlay
        startOverlay.setVisible(true);
        startOverlay.setEnabled(true);
        startOverlay.setFocusable(true);
        startOverlay.requestFocusInWindow();
        
        isAnimating = false;
    }
    
    /**
     * Hide start overlay
     */
    public void hideStart() {
        if (isAnimating) return;
        
        isAnimating = true;
        isStartVisible = false;
        
        startOverlay.setVisible(false);
        startOverlay.setEnabled(false);
        
        isAnimating = false;
    }
    
    /**
     * Show result banner with given text
     */
    public void showResult(String text) {
        if (isAnimating || isResultVisible) return; // Prevent double-shows
        
        isAnimating = true;
        isResultVisible = true;
        isStartVisible = false;
        
        // Hide start overlay first
        hideStart();
        
        // Set text and show result banner
        resultBanner.setResultText(text);
        resultBanner.showResult(text);
        
        // Start auto-hide timer
        autoHideTimer.stop();
        autoHideTimer.start();
        
        isAnimating = false;
    }
    
    /**
     * Hide result banner
     */
    public void hideResult() {
        if (isAnimating) return;
        
        isAnimating = true;
        isResultVisible = false;
        
        autoHideTimer.stop();
        resultBanner.dismiss();
        
        isAnimating = false;
    }
    
    /**
     * Hide all overlays
     */
    public void hideAll() {
        if (isAnimating) return;
        
        isAnimating = true;
        isStartVisible = false;
        isResultVisible = false;
        
        autoHideTimer.stop();
        startOverlay.setVisible(false);
        startOverlay.setEnabled(false);
        resultBanner.dismiss();
        
        isAnimating = false;
    }
    
    /**
     * Force hide all overlays (for new round)
     */
    public void forceHideAll() {
        isStartVisible = false;
        isResultVisible = false;
        isAnimating = false;
        
        autoHideTimer.stop();
        startOverlay.setVisible(false);
        startOverlay.setEnabled(false);
        resultBanner.dismiss();
    }
    
    /**
     * Update overlay positions after resize/fullscreen
     */
    public void updatePositions() {
        if (isResultVisible) {
            // Re-center result banner
            SwingUtilities.invokeLater(() -> {
                if (resultBanner.getParent() != null) {
                    Dimension parentSize = resultBanner.getParent().getSize();
                    Dimension bannerSize = resultBanner.getPreferredSize();
                    int x = (parentSize.width - bannerSize.width) / 2;
                    int y = (parentSize.height - bannerSize.height) / 2;
                    resultBanner.setBounds(x, y, bannerSize.width, bannerSize.height);
                }
            });
        }
    }
    
    // State getters for diagnostics
    public boolean isStartVisible() { return isStartVisible; }
    public boolean isResultVisible() { return isResultVisible; }
    public boolean isAnimating() { return isAnimating; }
}
