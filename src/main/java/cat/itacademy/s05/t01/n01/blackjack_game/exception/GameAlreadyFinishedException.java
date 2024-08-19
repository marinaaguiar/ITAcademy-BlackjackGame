package cat.itacademy.s05.t01.n01.blackjack_game.exception;

public class GameAlreadyFinishedException extends RuntimeException {
    public GameAlreadyFinishedException() {
        super("Game already finished.");
    }
}
