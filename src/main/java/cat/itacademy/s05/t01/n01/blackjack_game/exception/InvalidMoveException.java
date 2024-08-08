package cat.itacademy.s05.t01.n01.blackjack_game.exception;

public class InvalidMoveException extends RuntimeException {
    public InvalidMoveException(String message) {
        super(message);
    }
}
