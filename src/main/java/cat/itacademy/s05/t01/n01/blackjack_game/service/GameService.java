package cat.itacademy.s05.t01.n01.blackjack_game.service;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.Player;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.PlayerAction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GameService {

    Mono<Game> createSinglePlayerGame(String playerName);
    Mono<Game> startNewGame(List<String> playerIds);
    Mono<Game> makeMove(String gameId, PlayerAction playerAction, int amountBet);
    Flux<Player> getPlayerRankings();
    Mono<Player> changePlayerName(String playerId, String newName);
    Mono<Game> getGameDetails(String id);
    Mono<Void> deleteGame(String id);
}
