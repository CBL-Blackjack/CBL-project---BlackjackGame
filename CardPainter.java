package bj.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Static utility class for drawing placeholder playing cards
 */
public class CardPainter {
    
    // Card dimensions
    private static final int DEFAULT_CARD_WIDTH = 80;
    private static final int DEFAULT_CARD_HEIGHT = 120;
    
    // Colors
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color CARD_BLACK = Color.BLACK;
    private static final Color CARD_BLUE = new Color(0, 50, 150);
    private static final Color CARD_RED = Color.RED;
    
    // Spacing for overlapped cards
    private static final int CARD_OFFSET = 20;
    private static final int CARD_CORNER_RADIUS = 8;
    
    /**
     * Draw a single card
     * @param g2 Graphics2D context
     * @param x X position of card
     * @param y Y position of card
     * @param w Width of card
     * @param h Height of card
     * @param card The card to draw
     * @param faceDown Whether the card is face down
     */
    public static void drawCard(Graphics2D g2, int x, int y, int w, int h, bj.model.Card card, boolean faceDown) {
        // Create isolated graphics context for this card
        Graphics2D g2d = (Graphics2D) g2.create();
        try {
            // Set up rendering hints
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Create card shape and set clipping
            RoundRectangle2D cardShape = new RoundRectangle2D.Double(0, 0, w, h, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS);
            
            // Translate to card position and set clip
            g2d.translate(x, y);
            Shape originalClip = g2d.getClip();
            g2d.setClip(cardShape);
            
            if (faceDown) {
                drawFaceDownCard(g2d, 0, 0, w, h);
            } else {
                drawFaceUpCard(g2d, 0, 0, w, h, card);
            }
            
            // Restore original clip
            g2d.setClip(originalClip);
        } finally {
            g2d.dispose();
        }
    }
    
    /**
     * Draw a face-up card with rank and suit (using local coordinates 0,0 to w,h)
     */
    private static void drawFaceUpCard(Graphics2D g2, int x, int y, int w, int h, bj.model.Card card) {
        // Draw card background (local coordinates)
        g2.setColor(CARD_WHITE);
        RoundRectangle2D cardRect = new RoundRectangle2D.Double(x, y, w, h, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS);
        g2.fill(cardRect);
        
        // Draw card border
        g2.setColor(CARD_BLACK);
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(cardRect);
        
        // Get rank and suit information from the card
        String rank = card.getRank().getShortLabel();
        String suit = card.getSuit().getSymbol();
        
        // Determine suit color (red for hearts and diamonds)
        Color suitColor = card.getSuit().isRed() ? CARD_RED : CARD_BLACK;
        g2.setColor(suitColor);
        
        // Set font for text
        Font originalFont = g2.getFont();
        Font cardFont = new Font("Arial", Font.BOLD, 14);
        g2.setFont(cardFont);
        
        // Draw rank and suit at top-left (local coordinates)
        g2.drawString(rank, x + 8, y + 18);
        
        // Draw suit symbol at top-left (below rank)
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString(suit, x + 8, y + 32);
        
        // Draw rank and suit at bottom-right (rotated) - create isolated context for rotation
        g2.setFont(cardFont);
        Graphics2D g2Rotated = (Graphics2D) g2.create();
        try {
            g2Rotated.translate(x + w - 8, y + h - 8);
            g2Rotated.rotate(Math.PI);
            g2Rotated.drawString(rank, 0, -8);
            g2Rotated.setFont(new Font("Arial", Font.PLAIN, 12));
            g2Rotated.drawString(suit, 0, 6);
        } finally {
            g2Rotated.dispose();
        }
        
        // Draw center suit symbol
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        FontMetrics fm = g2.getFontMetrics();
        StringBounds centerSuit = new StringBounds(suit, fm);
        int centerX = x + w / 2 - centerSuit.width / 2;
        int centerY = y + h / 2 + centerSuit.height / 2;
        g2.drawString(suit, centerX, centerY);
        
        // Restore original font
        g2.setFont(originalFont);
    }
    
    /**
     * Draw a face-down card with a simple pattern (using local coordinates 0,0 to w,h)
     */
    private static void drawFaceDownCard(Graphics2D g2, int x, int y, int w, int h) {
        // Draw card background (local coordinates)
        g2.setColor(CARD_BLUE);
        RoundRectangle2D cardRect = new RoundRectangle2D.Double(x, y, w, h, CARD_CORNER_RADIUS, CARD_CORNER_RADIUS);
        g2.fill(cardRect);
        
        // Draw card border
        g2.setColor(CARD_WHITE);
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(cardRect);
        
        // Draw simple pattern (crosshatch) - using local coordinates only
        g2.setColor(new Color(100, 150, 255)); // Lighter blue
        g2.setStroke(new BasicStroke(1.5f));
        
        // Draw diagonal lines with proper bounds checking
        int spacing = 8;
        
        // Diagonal lines going up-right (from bottom-left to top-right)
        for (int i = 0; i < w + h; i += spacing) {
            int startX = Math.max(x, x + i - h);
            int startY = Math.min(y + h, y + h - i + w);
            int endX = Math.min(x + w, x + i);
            int endY = Math.max(y, y + h - i);
            
            if (startX < endX && startY > endY) {
                g2.drawLine(startX, startY, endX, endY);
            }
        }
        
        // Diagonal lines going down-right (from top-left to bottom-right)
        for (int i = 0; i < w + h; i += spacing) {
            int startX = Math.max(x, x - i);
            int startY = Math.max(y, y + i - w);
            int endX = Math.min(x + w, x - i + h);
            int endY = Math.min(y + h, y + i);
            
            if (startX < endX && startY < endY) {
                g2.drawLine(startX, startY, endX, endY);
            }
        }
        
        // Draw some dots for texture (local coordinates)
        g2.setColor(new Color(150, 200, 255));
        for (int i = x + 15; i < x + w - 15; i += 15) {
            for (int j = y + 15; j < y + h - 15; j += 15) {
                g2.fillOval(i, j, 3, 3);
            }
        }
    }
    
    /**
     * Draw a hand of overlapped cards
     * @param g2 Graphics2D context
     * @param anchor Starting position for the first card
     * @param cardCount Number of cards to draw
     */
    public static void drawHand(Graphics2D g2, Point anchor, int cardCount) {
        drawHand(g2, anchor, cardCount, DEFAULT_CARD_WIDTH, DEFAULT_CARD_HEIGHT);
    }
    
    /**
     * Draw a hand of overlapped cards with custom dimensions
     * @param g2 Graphics2D context
     * @param anchor Starting position for the first card
     * @param cardCount Number of cards to draw
     * @param cardWidth Width of each card
     * @param cardHeight Height of each card
     */
    public static void drawHand(Graphics2D g2, Point anchor, int cardCount, int cardWidth, int cardHeight) {
        if (cardCount <= 0) return;
        
        // For now, draw all cards as face-down placeholders
        // In a real game, you'd pass actual card data
        for (int i = 0; i < cardCount; i++) {
            int cardX = anchor.x + (i * CARD_OFFSET);
            int cardY = anchor.y;
            
            // Each card draw is isolated via drawCard method
            // Create a dummy card for placeholder (this should be replaced with actual cards)
            bj.model.Card dummyCard = new bj.model.Card(bj.model.Suit.SPADES, bj.model.Rank.ACE);
            drawCard(g2, cardX, cardY, cardWidth, cardHeight, dummyCard, true);
        }
    }
    
    /**
     * Draw a hand of specific cards
     * @param g2 Graphics2D context
     * @param anchor Starting position for the first card
     * @param cards Array of cards with face-down status
     */
    public static void drawHand(Graphics2D g2, Point anchor, CardData[] cards) {
        drawHand(g2, anchor, cards, DEFAULT_CARD_WIDTH, DEFAULT_CARD_HEIGHT);
    }
    
    /**
     * Draw a hand of specific cards with custom dimensions
     * @param g2 Graphics2D context
     * @param anchor Starting position for the first card
     * @param cards Array of cards with face-down status
     * @param cardWidth Width of each card
     * @param cardHeight Height of each card
     */
    public static void drawHand(Graphics2D g2, Point anchor, CardData[] cards, int cardWidth, int cardHeight) {
        if (cards == null || cards.length == 0) return;
        
        // Each card is drawn in isolation via drawCard method
        for (int i = 0; i < cards.length; i++) {
            int cardX = anchor.x + (i * CARD_OFFSET);
            int cardY = anchor.y;
            
            CardData cardData = cards[i];
            drawCard(g2, cardX, cardY, cardWidth, cardHeight, cardData.card, cardData.faceDown);
        }
    }
    
    /**
     * Simple data class for card information
     */
    public static class CardData {
        public final bj.model.Card card;
        public final boolean faceDown;
        
        public CardData(bj.model.Card card, boolean faceDown) {
            this.card = card;
            this.faceDown = faceDown;
        }
    }
    
    /**
     * Helper class for measuring string bounds
     */
    private static class StringBounds {
        public final int width;
        public final int height;
        
        public StringBounds(String text, FontMetrics fm) {
            this.width = fm.stringWidth(text);
            this.height = fm.getHeight();
        }
    }
}
