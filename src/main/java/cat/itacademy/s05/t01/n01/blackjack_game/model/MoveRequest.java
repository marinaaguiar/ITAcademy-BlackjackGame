package cat.itacademy.s05.t01.n01.blackjack_game.model;

import cat.itacademy.s05.t01.n01.blackjack_game.utils.PlayerAction;

public class MoveRequest {
    private PlayerAction playerMove;
    private int amountBet;

    public PlayerAction getMoveType() {
        return playerMove;
    }

    public void setMoveType(PlayerAction playerMove) {
        this.playerMove = playerMove;
    }

    public int getAmountBet() {
        return amountBet;
    }

    public void setAmountBet(int amountBet) {
        this.amountBet = amountBet;
    }
}
