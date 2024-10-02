package cat.itacademy.s05.t01.n01.blackjack_game.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "games")
public class Game {

    @Id
    private String id;
    private List<PlayerState> playersState;
    private GameState gameState;
    private List<String> deck;
    private List<String> dealerHand;
    private int dealerScore;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PlayerState> getPlayersState() {
        return playersState;
    }

    public void setPlayersState(List<PlayerState> playersState) {
        this.playersState = playersState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public List<String> getDeck() {
        return deck;
    }

    public void setDeck(List<String> deck) {
        this.deck = deck;
    }

    public List<String> getDealerHand() {
        return dealerHand;
    }

    public void setDealerHand(List<String> dealerHand) {
        this.dealerHand = dealerHand;
    }

    public int getDealerScore() {
        return dealerScore;
    }

    public void setDealerScore(int dealerScore) {
        this.dealerScore = dealerScore;
    }
}
