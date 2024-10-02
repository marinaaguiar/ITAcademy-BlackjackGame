package cat.itacademy.s05.t01.n01.blackjack_game.service;

import cat.itacademy.s05.t01.n01.blackjack_game.exception.InvalidMoveException;
import cat.itacademy.s05.t01.n01.blackjack_game.exception.PlayerNotFoundException;
import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.Player;
import cat.itacademy.s05.t01.n01.blackjack_game.model.PlayerState;
import cat.itacademy.s05.t01.n01.blackjack_game.repository.GameRepository;
import cat.itacademy.s05.t01.n01.blackjack_game.repository.PlayerRepository;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.CardUtils;
import cat.itacademy.s05.t01.n01.blackjack_game.model.GameState;
import cat.itacademy.s05.t01.n01.blackjack_game.model.PlayerAction;
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
    private final GameActionInteractor gameActionInteractor;

    @Autowired
    public GameManager(
            GameRepository gameRepository,
            PlayerRepository playerRepository,
            GameActionInteractor gameActionInteractor) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.gameActionInteractor = gameActionInteractor;
    }

    @Override
    public Mono<Game> createSinglePlayerGame(String playerName) {
        Player player = new Player(playerName, 0);

        return playerRepository.save(player)
                .flatMap(savedPlayer -> {
                    Game game = new Game();
                    game.setId(UUID.randomUUID().toString());
                    game.setGameState(GameState.ONGOING);

                    PlayerState playerState = new PlayerState(savedPlayer.getId());
                    playerState.setHand(new ArrayList<>());
                    playerState.setScore(0);

                    game.setPlayersState(new ArrayList<>(List.of(playerState)));
                    game.setDealerHand(new ArrayList<>());
                    game.setDealerScore(0);

                    gameActionInteractor.initializeGame(game); // Initialize with 6 decks

                    return gameRepository.save(game);
                });
    }

    @Override
    public Mono<Game> startNewGame(List<String> playerIds) {
        return playerRepository.findAllById(playerIds)
                .collectList()
                .flatMap(players -> {
                    if (players.size() != playerIds.size()) {
                        return Mono.error(new PlayerNotFoundException());
                    }

                    Game game = new Game();
                    game.setId(UUID.randomUUID().toString());
                    game.setGameState(GameState.ONGOING);

                    List<PlayerState> playerStates = players.stream()
                            .map(player -> new PlayerState(player.getId()))
                            .collect(Collectors.toList());

                    game.setPlayersState(new ArrayList<>(playerStates));
                    game.setDealerHand(new ArrayList<>());
                    game.setDealerScore(0);

                    gameActionInteractor.initializeGame(game); // Initialize with 6 decks

                    return Flux.fromIterable(playerStates)
                            .flatMap(playerState -> gameActionInteractor.dealCard(game)
                                    .flatMap(card1 -> {
                                        playerState.getHand().add(card1);
                                        return gameActionInteractor.dealCard(game);
                                    })
                                    .doOnNext(card2 -> {
                                        playerState.getHand().add(card2);
                                        playerState.setScore(CardUtils.calculateHandValue(playerState.getHand()));
                                    }))
                            .then(Mono.just(game))
                            .flatMap(gameRepository::save);
                });
    }

    @Override
    public Mono<Game> makeMove(String gameId, PlayerAction playerAction, int amountBet) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new GameNotFoundException("Game not found with id: " + gameId)))
                .flatMap(game -> {
                    if (game.getPlayersState() == null || game.getPlayersState().isEmpty()) {
                        return Mono.error(new IllegalStateException("No player states found for game id: " + gameId));
                    }

                    PlayerState playerState = game.getPlayersState().getFirst();
                    if (playerState == null) {
                        return Mono.error(new IllegalStateException("Player state is null for game id: " + gameId));
                    }

                    Mono<Game> actionResult = Mono.error(new InvalidMoveException("Invalid move type: " + playerAction));

                    switch (playerAction) {
                        case HIT:
                            actionResult = gameActionInteractor.hit(game.getId(), playerState.getPlayerId());
                            break;
                        case STANDING:
                            actionResult = gameActionInteractor.stand(game.getId(), playerState.getPlayerId());
                            break;
                        case DOUBLED_DOWN:
                            actionResult = gameActionInteractor.doubleDown(game.getId(), playerState.getPlayerId(), amountBet);
                            break;
                        case SURRENDERED:
                            actionResult = gameActionInteractor.surrender(game.getId(), playerState.getPlayerId());
                            break;
                    }

                    return actionResult
                            .switchIfEmpty(Mono.error(new IllegalStateException("No action result found for playerAction: " + playerAction)))
                            .doOnNext(result -> System.out.println("Action result: " + result))
                            .doOnError(error -> System.err.println("Error during action: " + error.getMessage()));
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Game not found")))
                .doOnError(error -> System.err.println("Error in makeMove: " + error.getMessage()));
    }

    @Override
    public Flux<Player> getPlayerRankings() {
        return playerRepository.findAll()
                .sort((p1, p2) -> {
                    System.out.println("Comparing scores: " + p1.getScore() + " and " + p2.getScore());
                    return Integer.compare(p2.getScore(), p1.getScore());
                })
                .doOnError(e -> System.err.println("Error fetching player rankings: " + e.getMessage()));
    }

    @Override
    public Mono<Player> changePlayerName(String playerId, String newName) {
        return playerRepository.findById(playerId)
                .flatMap(player -> {
                    player.setName(newName);
                    return playerRepository.save(player);
                })
                .switchIfEmpty(Mono.error(new PlayerNotFoundException()));
    }

    @Override
    public Mono<Game> getGameDetails(String id) {
        return gameRepository.findById(id)
                .switchIfEmpty(Mono.error(new GameNotFoundException("Game not found with id: " + id)));
    }

    @Override
    public Mono<Void> deleteGame(String id) {
        return gameRepository.findById(id)
                .flatMap(game -> {
                    System.out.println("Found game: " + game.getId());
                    return gameRepository.delete(game)
                            .doOnSuccess(unused -> System.out.println("Game deleted successfully with ID: " + id))
                            .doOnError(e -> System.err.println("Error during deletion: " + e.getMessage()));
                })
                .switchIfEmpty(Mono.error(new GameNotFoundException("Game not found with id: " + id)));
    }
}