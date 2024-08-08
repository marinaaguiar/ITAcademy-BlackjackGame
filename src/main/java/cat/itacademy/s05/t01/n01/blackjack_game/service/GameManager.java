package cat.itacademy.s05.t01.n01.blackjack_game.service;

import cat.itacademy.s05.t01.n01.blackjack_game.exception.InvalidMoveException;
import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.Player;
import cat.itacademy.s05.t01.n01.blackjack_game.model.PlayerState;
import cat.itacademy.s05.t01.n01.blackjack_game.repository.GameRepository;
import cat.itacademy.s05.t01.n01.blackjack_game.repository.PlayerRepository;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.CardUtils;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.GameState;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.PlayerAction;
import cat.itacademy.s05.t01.n01.blackjack_game.exception.GameNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameManager implements GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameActionService gameActionService;

    @Autowired
    public GameManager(
            GameRepository gameRepository,
            PlayerRepository playerRepository,
            GameActionService gameActionService) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.gameActionService = gameActionService;
    }

    @Override
    public Mono<Game> createSinglePlayerGame(String playerName) {
        Player player = new Player();
        player.setName(playerName);
        player.setScore(0);

        return playerRepository.save(player)
                .flatMap(savedPlayer -> {
                    Game game = new Game();
                    game.setID(UUID.randomUUID().toString());
                    game.setGameState(GameState.ONGOING);
                    PlayerState playerState = new PlayerState(savedPlayer.getId());
                    playerState.setHand(new ArrayList<>());
                    playerState.setScore(0);
                    game.setPlayerStates(new ArrayList<>(List.of(playerState)));
                    game.setDealerHand(new ArrayList<>());
                    game.setDealerScore(0);
                    game.setDeck(CardUtils.createShuffledDeck());

                    return gameRepository.save(game);
                });
    }

    @Override
    public Mono<Game> startNewGame(List<String> playerIds) {
        return playerRepository.findAllById(playerIds)
                .collectList()
                .flatMap(players -> {
                    if (players.size() != playerIds.size()) {
                        return Mono.error(new IllegalArgumentException("Some players not found"));
                    }

                    Game game = new Game();
                    game.setID(UUID.randomUUID().toString());
                    game.setGameState(GameState.ONGOING);

                    List<PlayerState> playerStates = players.stream()
                            .map(player -> new PlayerState(player.getId()))
                            .collect(Collectors.toList());

                    game.setPlayerStates(new ArrayList<>(playerStates));

                    return Flux.fromIterable(playerStates)
                            .flatMap(playerState -> gameActionService.dealCard(game)
                                    .flatMap(card1 -> {
                                        playerState.getHand().add(card1);
                                        return gameActionService.dealCard(game);
                                    })
                                    .doOnNext(card2 -> {
                                        playerState.getHand().add(card2);
                                        playerState.setScore(CardUtils.calculateHandValue(playerState.getHand()));
                                    }))
                            .then(Mono.just(game))
                            .flatMap(gameRepository::save);
                });
    }

    public Mono<Game> dealInitialCards(Game game) {
        return Flux.fromIterable(game.getPlayerStates())
                .flatMap(playerState -> gameActionService.dealCard(game)
                        .flatMap(card1 -> {
                            playerState.getHand().add(card1);
                            return gameActionService.dealCard(game).map(card2 -> {
                                playerState.getHand().add(card2);
                                return playerState;
                            });
                        })
                )
                .collectList()
                .map(updatedStates -> {
                    game.setPlayerStates(updatedStates);
                    game.getPlayerStates().forEach(state ->
                            state.setScore(CardUtils.calculateHandValue(state.getHand()))
                    );
                    return game;
                });
    }

    @Override
    public Mono<Game> makeMove(String gameId, PlayerAction playerAction, int amountBet) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new GameNotFoundException("Game not found with id: " + gameId)))
                .flatMap(game -> {
                    PlayerState playerState = game.getPlayerStates().getFirst();

                    switch (playerAction) {
                        case HIT:
                            return gameActionService.hit(game.getID(), playerState.getPlayerId());
                        case STANDING:
                            return gameActionService.stand(game.getID(), playerState.getPlayerId());
                        case DOUBLED_DOWN:
                            return gameActionService.doubleDown(game.getID(), playerState.getPlayerId(), amountBet);
                        case SURRENDERED:
                            return gameActionService.surrender(game.getID(), playerState.getPlayerId());
                        default:
                            return Mono.error(new InvalidMoveException("Invalid move type: " + playerAction));
                    }
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Game not found")));
    }

    @Override
    public Flux<Player> getPlayerRankings() {
        return playerRepository.findAll()
                .sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
    }

    @Override
    public Mono<Player> changePlayerName(String playerId, String newName) {
        return playerRepository.findById(playerId)
                .flatMap(player -> {
                    player.setName(newName);
                    return playerRepository.save(player);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Player not found")));
    }

    @Override
    public Mono<Game> getGameDetails(String id) {
        return gameRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Game not found")));
    }

    @Override
    public Mono<Void> deleteGame(String id) {
        return gameRepository.findById(id)
                .flatMap(game -> gameRepository.delete(game))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Game not found")));
    }
}