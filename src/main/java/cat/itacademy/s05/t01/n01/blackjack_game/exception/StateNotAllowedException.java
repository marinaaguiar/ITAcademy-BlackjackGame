package cat.itacademy.s05.t01.n01.blackjack_game.exception;

public class StateNotAllowedException extends RuntimeException {
    public StateNotAllowedException(String message) {
        super(message);
    }
}
