package bj.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Localization system for card names and game text.
 * Supports English (EN) and Turkish (TR) locales.
 */
public class Localization {
    
    public enum Locale {
        EN, TR
    }
    
    private static Locale currentLocale = Locale.EN;
    
    // English translations
    private static final Map<Rank, String> EN_RANK_NAMES = new HashMap<>();
    private static final Map<Suit, String> EN_SUIT_NAMES = new HashMap<>();
    
    // Turkish translations
    private static final Map<Rank, String> TR_RANK_NAMES = new HashMap<>();
    private static final Map<Suit, String> TR_SUIT_NAMES = new HashMap<>();
    
    static {
        // Initialize English names
        EN_RANK_NAMES.put(Rank.ACE, "Ace");
        EN_RANK_NAMES.put(Rank.KING, "King");
        EN_RANK_NAMES.put(Rank.QUEEN, "Queen");
        EN_RANK_NAMES.put(Rank.JACK, "Jack");
        EN_RANK_NAMES.put(Rank.TEN, "Ten");
        EN_RANK_NAMES.put(Rank.NINE, "Nine");
        EN_RANK_NAMES.put(Rank.EIGHT, "Eight");
        EN_RANK_NAMES.put(Rank.SEVEN, "Seven");
        EN_RANK_NAMES.put(Rank.SIX, "Six");
        EN_RANK_NAMES.put(Rank.FIVE, "Five");
        EN_RANK_NAMES.put(Rank.FOUR, "Four");
        EN_RANK_NAMES.put(Rank.THREE, "Three");
        EN_RANK_NAMES.put(Rank.TWO, "Two");
        
        EN_SUIT_NAMES.put(Suit.SPADES, "Spades");
        EN_SUIT_NAMES.put(Suit.HEARTS, "Hearts");
        EN_SUIT_NAMES.put(Suit.DIAMONDS, "Diamonds");
        EN_SUIT_NAMES.put(Suit.CLUBS, "Clubs");
        
        // Initialize Turkish names
        TR_RANK_NAMES.put(Rank.ACE, "As");
        TR_RANK_NAMES.put(Rank.KING, "Papaz");
        TR_RANK_NAMES.put(Rank.QUEEN, "Kız");
        TR_RANK_NAMES.put(Rank.JACK, "Vale");
        TR_RANK_NAMES.put(Rank.TEN, "On");
        TR_RANK_NAMES.put(Rank.NINE, "Dokuz");
        TR_RANK_NAMES.put(Rank.EIGHT, "Sekiz");
        TR_RANK_NAMES.put(Rank.SEVEN, "Yedi");
        TR_RANK_NAMES.put(Rank.SIX, "Altı");
        TR_RANK_NAMES.put(Rank.FIVE, "Beş");
        TR_RANK_NAMES.put(Rank.FOUR, "Dört");
        TR_RANK_NAMES.put(Rank.THREE, "Üç");
        TR_RANK_NAMES.put(Rank.TWO, "İki");
        
        TR_SUIT_NAMES.put(Suit.SPADES, "Maça");
        TR_SUIT_NAMES.put(Suit.HEARTS, "Kupa");
        TR_SUIT_NAMES.put(Suit.DIAMONDS, "Karo");
        TR_SUIT_NAMES.put(Suit.CLUBS, "Sinek");
    }
    
    /**
     * Set the current locale.
     * @param locale The locale to use (EN or TR)
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
    }
    
    /**
     * Get the current locale.
     * @return The current locale
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }
    
    /**
     * Get the localized name for a rank.
     * @param rank The rank to get the name for
     * @return The localized rank name
     */
    public static String getRankName(Rank rank) {
        return getRankName(rank, currentLocale);
    }
    
    /**
     * Get the localized name for a rank in a specific locale.
     * @param rank The rank to get the name for
     * @param locale The locale to use
     * @return The localized rank name
     */
    public static String getRankName(Rank rank, Locale locale) {
        switch (locale) {
            case TR:
                return TR_RANK_NAMES.get(rank);
            case EN:
            default:
                return EN_RANK_NAMES.get(rank);
        }
    }
    
    /**
     * Get the localized name for a suit.
     * @param suit The suit to get the name for
     * @return The localized suit name
     */
    public static String getSuitName(Suit suit) {
        return getSuitName(suit, currentLocale);
    }
    
    /**
     * Get the localized name for a suit in a specific locale.
     * @param suit The suit to get the name for
     * @param locale The locale to use
     * @return The localized suit name
     */
    public static String getSuitName(Suit suit, Locale locale) {
        switch (locale) {
            case TR:
                return TR_SUIT_NAMES.get(suit);
            case EN:
            default:
                return EN_SUIT_NAMES.get(suit);
        }
    }
    
    /**
     * Get the full localized card name (e.g., "Ace of Spades" or "As Maça").
     * @param card The card to get the name for
     * @return The full localized card name
     */
    public static String getCardName(Card card) {
        return getCardName(card, currentLocale);
    }
    
    /**
     * Get the full localized card name in a specific locale.
     * @param card The card to get the name for
     * @param locale The locale to use
     * @return The full localized card name
     */
    public static String getCardName(Card card, Locale locale) {
        String rankName = getRankName(card.getRank(), locale);
        String suitName = getSuitName(card.getSuit(), locale);
        
        if (locale == Locale.TR) {
            return rankName + " " + suitName;
        } else {
            return rankName + " of " + suitName;
        }
    }
}
