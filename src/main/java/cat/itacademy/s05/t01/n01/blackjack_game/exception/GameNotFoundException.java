package cat.itacademy.s05.t01.n01.blackjack_game.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String message) {
        super(message);
    }
}
