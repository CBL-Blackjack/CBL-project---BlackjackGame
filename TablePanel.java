package bj.ui;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Collections;
import bj.model.Card;

/**
 * A custom JPanel that renders a blackjack table with felt-like background
 * and defined areas for betting, player cards, and dealer cards.
 * 
 * <p>This class uses only Swing/AWT components and is designed to be robust
 * on window resize. The bet center and anchor points are computed fresh from
 * current dimensions each time to ensure proper positioning after resizing.</p>
 * 
 * <p><b>Swing-only constraint:</b deployed without external libraries.</p>
 */
public class TablePanel extends JPanel {
    
    // Cached shapes for external access
    private Rectangle playerArea;
    private Rectangle dealerArea;
    private Ellipse2D betCircle;
    
    // Cached bet center for external access (updated on each paint)
    private Point cachedBetCenter;
    
    // Pluggable animator for chip drawing
    private TableAnimator animator;
    
    // Live hand data for rendering
    private List<Card> playerCards = Collections.emptyList();
    private List<Card> dealerCards = Collections.emptyList();
    private boolean hideDealerHole = true;
    
    // Overlay text for totals
    private String playerTotalText = "";
    private String dealerTotalText = "";
    private String betText = "";
    
    // Colors
    private static final Color FELT_GREEN = new Color(18, 92, 60);
    private static final Color LIGHT_RING_COLOR = new Color(200, 200, 200);
    
    // Padding and margins
    private static final int MARGIN = 40;
    private static final int CORNER_RADIUS = 20;
    
    public TablePanel() {
        setPreferredSize(new Dimension(800, 600));
        setOpaque(true); // Ensure the felt background is painted
        // Temporary diagnostic border
        setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        // Initialize with reasonable defaults to prevent NullPointerExceptions
        cachedBetCenter = new Point(400, 400); // Default center
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Enable antialiasing
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Fill background with felt green
        g2d.setColor(FELT_GREEN);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Calculate and cache shapes based on current dimensions
        calculateShapes();
        
        // Draw the three areas
        drawBetArea(g2d);
        drawPlayerCardsArea(g2d);
        drawDealerCardsArea(g2d);
        
        // Draw chips if animator is set
        if (animator != null) {
            animator.drawAll(g2d);
        }
        
        // Draw placeholder cards for dealer and player
        drawPlaceholderCards(g2d);
        
        // Draw overlay text for totals
        drawOverlayTexts(g2d);
        
        // Draw bet text
        drawBetText(g2d);
        
        g2d.dispose();
    }
    
    /**
     * Calculate shapes based on current panel dimensions
     */
    private void calculateShapes() {
        int width = getWidth();
        int height = getHeight();
        
        if (width <= 0 || height <= 0) {
            return;
        }
        
        // Bet circle - centered at bottom third of the panel
        int betCenterY = height * 2 / 3;
        int betRadius = Math.min(width, height) / 8;
        int betX = width / 2 - betRadius;
        int betY = betCenterY - betRadius;
        betCircle = new Ellipse2D.Double(betX, betY, betRadius * 2, betRadius * 2);
        
        // Cache the bet center for external access
        cachedBetCenter = new Point(width / 2, betCenterY);
        
        // Player cards area - bottom half, excluding bet area
        int playerAreaY = height / 2 + MARGIN;
        int playerAreaHeight = height / 2 - MARGIN * 2;
        playerArea = new Rectangle(MARGIN, playerAreaY, width - MARGIN * 2, playerAreaHeight);
        
        // Dealer cards area - upper half
        int dealerAreaHeight = height / 2 - MARGIN * 2;
        dealerArea = new Rectangle(MARGIN, MARGIN, width - MARGIN * 2, dealerAreaHeight);
    }
    
    /**
     * Draw the circular bet area with a light ring stroke
     */
    private void drawBetArea(Graphics2D g2d) {
        if (betCircle == null) return;
        
        g2d.setColor(LIGHT_RING_COLOR);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.draw(betCircle);
    }
    
    /**
     * Draw the player cards area as a rounded rectangle
     */
    private void drawPlayerCardsArea(Graphics2D g2d) {
        if (playerArea == null) return;
        
        RoundRectangle2D playerRect = new RoundRectangle2D.Double(
            playerArea.x, playerArea.y, 
            playerArea.width, playerArea.height, 
            CORNER_RADIUS, CORNER_RADIUS
        );
        
        g2d.setColor(LIGHT_RING_COLOR);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(playerRect);
    }
    
    /**
     * Draw the dealer cards area as a rounded rectangle
     */
    private void drawDealerCardsArea(Graphics2D g2d) {
        if (dealerArea == null) return;
        
        RoundRectangle2D dealerRect = new RoundRectangle2D.Double(
            dealerArea.x, dealerArea.y, 
            dealerArea.width, dealerArea.height, 
            CORNER_RADIUS, CORNER_RADIUS
        );
        
        g2d.setColor(LIGHT_RING_COLOR);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(dealerRect);
    }
    
    /**
     * Draw placeholder cards for dealer and player hands
     */
    private void drawPlaceholderCards(Graphics2D g2d) {
        // Draw dealer cards using live hand data
        Point dealerAnchor = getDealerCardsAnchor();
        if (!dealerCards.isEmpty()) {
            // Convert dealer cards to CardData array
            CardPainter.CardData[] dealerCardData = new CardPainter.CardData[dealerCards.size()];
            for (int i = 0; i < dealerCards.size(); i++) {
                Card card = dealerCards.get(i);
                boolean faceDown = false;
                
                // First card is always face up, second card is face down if hideDealerHole is true
                if (i == 1 && hideDealerHole) {
                    faceDown = true;
                }
                
                dealerCardData[i] = new CardPainter.CardData(
                    card,
                    faceDown
                );
            }
            CardPainter.drawHand(g2d, dealerAnchor, dealerCardData);
        }
        
        // Draw player cards using live hand data
        Point playerAnchor = getPlayerCardsAnchor();
        if (!playerCards.isEmpty()) {
            // Convert player cards to CardData array (all face up)
            CardPainter.CardData[] playerCardData = new CardPainter.CardData[playerCards.size()];
            for (int i = 0; i < playerCards.size(); i++) {
                Card card = playerCards.get(i);
                playerCardData[i] = new CardPainter.CardData(
                    card,
                    false // Player cards are always face up
                );
            }
            CardPainter.drawHand(g2d, playerAnchor, playerCardData);
        }
    }
    
    /**
     * Draw overlay texts for player and dealer totals
     */
    private void drawOverlayTexts(Graphics2D g2d) {
        // Set font for overlay text
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 16f));
        
        // Draw player text near player area top-left
        if (!playerTotalText.isEmpty()) {
            Point playerAnchor = getPlayerCardsAnchor();
            
            // Check if player is bust and highlight in red
            if (playerTotalText.contains("Total:") && extractValue(playerTotalText) > 21) {
                g2d.setColor(new Color(255, 100, 100, 220)); // red highlight for bust
            } else {
                g2d.setColor(new Color(255, 255, 255, 200)); // normal white
            }
            g2d.drawString(playerTotalText, playerAnchor.x, playerAnchor.y - 10);
        }
        
        // Draw dealer text near dealer area top-left
        if (!dealerTotalText.isEmpty()) {
            Point dealerAnchor = getDealerCardsAnchor();
            
            // Check if dealer is bust and highlight in red
            if (dealerTotalText.contains("Total:") && extractValue(dealerTotalText) > 21) {
                g2d.setColor(new Color(255, 100, 100, 220)); // red highlight for bust
            } else {
                g2d.setColor(new Color(255, 255, 255, 200)); // normal white
            }
            g2d.drawString(dealerTotalText, dealerAnchor.x, dealerAnchor.y - 10);
        }
    }
    
    /**
     * Extract numeric value from total text for bust detection
     */
    private int extractValue(String totalText) {
        try {
            // Look for "Total: X" pattern and extract the number
            if (totalText.startsWith("Total: ")) {
                String numberPart = totalText.substring(7); // Skip "Total: "
                // Remove "(soft)" suffix if present
                if (numberPart.contains(" (soft)")) {
                    numberPart = numberPart.substring(0, numberPart.indexOf(" (soft)"));
                }
                return Integer.parseInt(numberPart.trim());
            }
        } catch (Exception e) {
            // If parsing fails, return 0 (not bust)
        }
        return 0;
    }
    
    
    // Getter methods for shapes
    
    /**
     * Get the player cards area rectangle
     * @return Rectangle representing the player cards area
     */
    public Rectangle getPlayerArea() {
        return playerArea;
    }
    
    /**
     * Get the dealer cards area rectangle
     * @return Rectangle representing the dealer cards area
     */
    public Rectangle getDealerArea() {
        return dealerArea;
    }
    
    /**
     * Get the bet circle ellipse
     * @return Ellipse2D representing the bet circle
     */
    public Ellipse2D getBetCircle() {
        return betCircle;
    }
    
    // Anchor point methods
    
    /**
     * Get the center point of the bet circle.
     * Computes fresh from current width/height each time for resize robustness.
     * 
     * <p>This method ensures that the bet center position is always accurate
     * even after window resizing, making it safe for external code to use
     * for chip targeting.</p>
     * 
     * @return Point representing the center of the bet circle
     */
    public Point getBetCenter() {
        // Compute fresh from current dimensions
        int width = getWidth();
        int height = getHeight();
        
        if (width <= 0 || height <= 0) {
            return cachedBetCenter != null ? cachedBetCenter : new Point(400, 400);
        }
        
        int betCenterY = height * 2 / 3;
        int betCenterX = width / 2;
        
        return new Point(betCenterX, betCenterY);
    }
    
    /**
     * Get the anchor point for player cards (center-left of player area)
     * @return Point representing where player cards should be positioned
     */
    public Point getPlayerCardsAnchor() {
        // Compute fresh from current dimensions for robustness on resize
        int width = getWidth();
        int height = getHeight();
        
        if (width <= 0 || height <= 0) {
            return new Point(MARGIN + 50, 450); // Default fallback
        }
        
        int playerAreaY = height / 2 + MARGIN;
        return new Point(MARGIN + 50, playerAreaY + (height / 2 - MARGIN * 2) / 2);
    }
    
    /**
     * Get the anchor point for dealer cards (center-left of dealer area)
     * @return Point representing where dealer cards should be positioned
     */
    public Point getDealerCardsAnchor() {
        // Compute fresh from current dimensions for robustness on resize
        int width = getWidth();
        int height = getHeight();
        
        if (width <= 0 || height <= 0) {
            return new Point(MARGIN + 50, 150); // Default fallback
        }
        
        int dealerAreaHeight = height / 2 - MARGIN * 2;
        return new Point(MARGIN + 50, MARGIN + dealerAreaHeight / 2);
    }
    
    // Animator management
    
    /**
     * Set the animator for chip drawing.
     * 
     * <p>This method establishes the connection between the table panel
     * and the chip animation system, allowing the panel to render
     * animated chips during its paint cycle.</p>
     * 
     * @param animator The TableAnimator to use for drawing chips
     */
    public void setAnimator(TableAnimator animator) {
        this.animator = animator;
    }
    
    /**
     * Get the current animator
     * @return The current TableAnimator, or null if none is set
     */
    public TableAnimator getAnimator() {
        return animator;
    }
    
    /**
     * Get the cached bet center (updated on each paint)
     * @return The cached bet center point
     */
    public Point getCachedBetCenter() {
        return cachedBetCenter != null ? new Point(cachedBetCenter) : new Point(400, 400);
    }
    
    /**
     * Set the live hand data for rendering.
     * @param player The player's cards
     * @param dealer The dealer's cards
     * @param hideDealerHole Whether to hide the dealer's hole card (second card)
     */
    public void setHands(List<Card> player, List<Card> dealer, boolean hideDealerHole) {
        this.playerCards = player != null ? player : Collections.emptyList();
        this.dealerCards = dealer != null ? dealer : Collections.emptyList();
        this.hideDealerHole = hideDealerHole;
        repaint();
    }
    
    /**
     * Set the overlay text for player and dealer totals.
     * @param playerText The text to display for player total
     * @param dealerText The text to display for dealer total
     */
    public void setTotals(String playerText, String dealerText) {
        this.playerTotalText = playerText;
        this.dealerTotalText = dealerText;
        repaint();
    }
    
    /**
     * Set bet text display
     */
    public void setBetText(String betText) {
        this.betText = betText != null ? betText : "";
        repaint();
    }
    
    /**
     * Draw bet text near the bet circle
     */
    private void drawBetText(Graphics2D g2d) {
        if (betText.isEmpty()) {
            return;
        }
        
        // Set font and color for bet text
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 14f));
        g2d.setColor(new Color(255, 255, 255, 220)); // White with transparency
        
        // Position bet text near the bet circle
        Point betCenter = getBetCenter();
        if (betCenter != null) {
            FontMetrics fm = g2d.getFontMetrics();
            int textX = betCenter.x - fm.stringWidth(betText) / 2;
            int textY = betCenter.y - 30; // Above the bet circle
            
            // Draw text shadow
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawString(betText, textX + 1, textY + 1);
            
            // Draw main text
            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.drawString(betText, textX, textY);
        }
    }
}
