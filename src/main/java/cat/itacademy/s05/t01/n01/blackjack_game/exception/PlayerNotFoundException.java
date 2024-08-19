package cat.itacademy.s05.t01.n01.blackjack_game.exception;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException() {
        super("Player not found.");
    }
}
