package cat.itacademy.s05.t01.n01.blackjack_game.model;

import cat.itacademy.s05.t01.n01.blackjack_game.utils.GameState;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "games")
public class Game {

    @Id
    private String id;
    private List<Player> players;
    private List<PlayerState> playersState;
    private GameState gameState;
    private List<String> deck;
    private String dealerId;
    private List<String> dealerHand;
    private int dealerScore;

    public void setID(String id) {
        this.id = id;
    }

    public void setPlayers(List<Player> playerList) {
        this.players = playerList;
    }

    public void setPlayerStates(List<PlayerState> playersState) {
        this.playersState = playersState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void setDeck(List<String> shuffledDeck) {
        this.deck = shuffledDeck;
    }

    public void setDealerHand(List<String> dealerHand) {
        this.dealerHand = dealerHand;
    }

    public void setDealerScore(int dealerScore) {
        this.dealerScore = dealerScore;
    }

    public GameState getGameState() {
        return gameState;
    }

    public List<String> getDeck() {
        return deck;
    }

    public List<PlayerState> getPlayerStates() {
        return playersState;
    }

    public int getDealerScore() {
        return dealerScore;
    }

    public List<String> getDealerHand() {
        return dealerHand;
    }

    public String getID() {
        return id;
    }

    public void setPlayersState(List<PlayerState> playerStates) {
        this.playersState = playerStates;
    }

    public List<PlayerState> getPlayersState() {
        return playersState;
    }
}
