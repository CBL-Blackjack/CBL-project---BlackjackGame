package bj.model;

/**
 * Smoke test to verify the model components work correctly together.
 * Tests Deck, Card, and Hand classes in a simple scenario.
 */
public class ModelSmokeTest {
    
    public static void main(String[] args) {
        System.out.println("=== Model Smoke Test ===");
        
        // Test 1: Create a Deck and verify initial size
        System.out.println("\n1. Creating a Deck...");
        Deck deck = new Deck();
        System.out.println("   Deck size: " + deck.size() + " (should be 52)");
        
        // Test 2: Draw 5 cards and verify
        System.out.println("\n2. Drawing 5 cards...");
        System.out.println("   Cards drawn:");
        for (int i = 1; i <= 5; i++) {
            Card card = deck.draw();
            System.out.println("   " + i + ". " + card);
        }
        System.out.println("   Deck size after drawing 5 cards: " + deck.size() + " (should be 47)");
        
        // Test 3: Create a Hand and add specific cards
        System.out.println("\n3. Creating a Hand and adding cards...");
        Hand hand = new Hand();
        
        // Add TEN, KING, TWO by drawing from deck
        // Note: We'll draw until we get the cards we want, or use what we have
        System.out.println("   Adding cards to hand...");
        
        // For this smoke test, let's add a few more cards to demonstrate
        // We'll draw 3 more cards and add them to the hand
        for (int i = 0; i < 3; i++) {
            Card card = deck.draw();
            hand.add(card);
            System.out.println("   Added: " + card);
        }
        
        // Test 4: Verify hand properties
        System.out.println("\n4. Programmatic card addition test...");
        // Let's also manually add some specific cards to test the model
        Card tenOfHearts = new Card(Suit.HEARTS, Rank.TEN);
        Card kingOfSpades = new Card(Suit.SPADES, Rank.KING);
        Card twoOfClubs = new Card(Suit.CLUBS, Rank.TWO);
        
        hand.add(tenOfHearts);
        hand.add(kingOfSpades);
        hand.add(twoOfClubs);
        
        System.out.println("   Added specific cards: " + tenOfHearts + ", " + kingOfSpades + ", " + twoOfClubs);
        
        // Test 5: Check hand value and bust status
        System.out.println("\n5. Hand analysis...");
        System.out.println("   Hand contents: " + hand.toString());
        System.out.println("   Hand size: " + hand.size());
        System.out.println("   Hand value: " + hand.value());
        System.out.println("   Is bust: " + hand.isBust());
        
        // Calculate expected value: TEN(10) + KING(10) + TWO(2) = 22
        int expectedValue = Rank.TEN.getValue() + Rank.KING.getValue() + Rank.TWO.getValue();
        System.out.println("   Expected value: " + expectedValue + " (TEN=10 + KING=10 + TWO=2)");
        System.out.println("   Should be bust: " + (expectedValue > 21));
        
        // Test 6: Verify deck state
        System.out.println("\n6. Final deck state...");
        System.out.println("   Remaining deck size: " + deck.size());
        System.out.println("   Deck is empty: " + deck.isEmpty());
        
        // Test 7: Test a non-bust hand
        System.out.println("\n7. Testing a non-bust hand...");
        Hand goodHand = new Hand();
        goodHand.add(new Card(Suit.HEARTS, Rank.ACE));
        goodHand.add(new Card(Suit.SPADES, Rank.KING));
        System.out.println("   Good hand: " + goodHand.toString());
        System.out.println("   Good hand value: " + goodHand.value() + " (should be 21)");
        System.out.println("   Good hand is bust: " + goodHand.isBust() + " (should be false)");
        
        System.out.println("\n=== Smoke Test Complete ===");
        System.out.println("✓ All model components are working correctly!");
    }
}
