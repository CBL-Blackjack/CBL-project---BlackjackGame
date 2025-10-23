package bj.logic;

/**
 * Represents the different states of a blackjack game round.
 * Defines the flow of game progression from round start to round end.
 */
public enum GameState {
    ROUND_START,
    BET_PLACED,
    PLAYER_TURN,
    DEALER_TURN,
    ROUND_END
}
