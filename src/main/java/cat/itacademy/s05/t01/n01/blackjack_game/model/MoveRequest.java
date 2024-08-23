package cat.itacademy.s05.t01.n01.blackjack_game.model;

public class MoveRequest {
    private PlayerAction playerAction;
    private int amountBet;

    public MoveRequest(PlayerAction playerAction, int amountBet) {
        this.playerAction = playerAction;
        this.amountBet = amountBet;
    }

    public PlayerAction getMoveType() {
        return playerAction;
    }

    public void setMoveType(PlayerAction playerMove) {
        this.playerAction = playerMove;
    }

    public int getAmountBet() {
        return amountBet;
    }

    public void setAmountBet(int amountBet) {
        this.amountBet = amountBet;
    }
}
