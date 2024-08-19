package cat.itacademy.s05.t01.n01.blackjack_game.exception;

public class DeckEmptyException extends RuntimeException {
    public DeckEmptyException() {
        super("No more cards in the deck");
    }
}
