package cat.itacademy.s05.t01.n01.blackjack_game.model;

import java.util.ArrayList;
import java.util.List;

public class PlayerState {
    private String playerId;
    private List<String> hand;
    private int score;
    private PlayerAction action;

    public PlayerState(String playerId) {
        this.playerId = playerId;
        this.action = PlayerAction.PLAYING;
        this.hand = new ArrayList<>();
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setAction(PlayerAction playerAction) {
        this.action = playerAction;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setHand(List<String> hand) {
        this.hand = hand;
    }

    public String getPlayerId() {
        return playerId;
    }

    public PlayerAction getAction() {
        return action;
    }

    public int getScore() {
        return score;
    }

    public List<String> getPlayerHand() {
        return hand;
    }

    @Override
    public String toString() {
        return "PlayerState{" +
                "playerId=" + playerId +
                ", hand=" + hand +
                ", score=" + score +
                ", action=" + action +
                '}';
    }
}