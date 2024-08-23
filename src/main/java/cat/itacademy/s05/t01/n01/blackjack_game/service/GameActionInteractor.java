package cat.itacademy.s05.t01.n01.blackjack_game.service;

import cat.itacademy.s05.t01.n01.blackjack_game.exception.DeckEmptyException;
import cat.itacademy.s05.t01.n01.blackjack_game.exception.GameAlreadyFinishedException;
import cat.itacademy.s05.t01.n01.blackjack_game.exception.PlayerNotFoundException;
import cat.itacademy.s05.t01.n01.blackjack_game.exception.StateNotAllowedException;
import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.PlayerState;
import cat.itacademy.s05.t01.n01.blackjack_game.repository.GameRepository;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.CardUtils;
import cat.itacademy.s05.t01.n01.blackjack_game.model.GameState;
import cat.itacademy.s05.t01.n01.blackjack_game.model.PlayerAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GameActionInteractor {

    private final GameRepository gameRepository;

    @Autowired
    public GameActionInteractor(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public void initializeGame(Game game) {
        List<String> deck = CardUtils.createShuffledDeck(6); // Use 6 decks shuffled together
        game.setDeck(deck);
    }

    public Mono<String> dealCard(Game game) {
        if (game.getDeck().isEmpty()) {
            return Mono.error(new DeckEmptyException());
        }
        return Mono.just(game.getDeck().remove(0));
    }

    public Mono<Game> hit(String gameId, int playerId) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new GameAlreadyFinishedException());
                    }

                    return Mono.justOrEmpty(game.getPlayerStates().stream()
                                    .filter(ps -> ps.getPlayerId() == playerId)
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new StateNotAllowedException("Player cannot hit in current state"));
                                }

                                return dealCard(game)
                                        .flatMap(card -> {
                                            playerState.getHand().add(card);
                                            playerState.setScore(CardUtils.calculateHandValue(playerState.getHand()));
                                            if (playerState.getScore() > 21) {
                                                playerState.setAction(PlayerAction.BUSTED);
                                            }
                                            return gameRepository.save(game);
                                        });
                            })
                            .switchIfEmpty(Mono.error(new PlayerNotFoundException()));
                });
    }

    public Mono<Game> stand(String gameId, int playerId) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new GameAlreadyFinishedException());
                    }

                    System.out.println("Game State before stand: " + game.getGameState());
                    System.out.println("Player States before stand: " + game.getPlayersState());

                    return Mono.justOrEmpty(game.getPlayersState().stream()
                                    .filter(ps -> ps.getPlayerId() == playerId)
                                    .findFirst())
                            .flatMap(playerState -> {
                                System.out.println("Found PlayerState: " + playerState);

                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new StateNotAllowedException("Player cannot stand in current state"));
                                }

                                playerState.setAction(PlayerAction.STANDING);

                                System.out.println("Updated PlayerState after standing: " + playerState);

                                if (game.getPlayersState().stream()
                                        .allMatch(ps -> ps.getAction() != PlayerAction.PLAYING)) {
                                    System.out.println("All players have finished their actions. Finishing game...");
                                    return finishGame(game);
                                }

                                return gameRepository.save(game);
                            })
                            .switchIfEmpty(Mono.error(new PlayerNotFoundException()));
                });
    }

    public Mono<Game> doubleDown(String gameId, int playerId, int amountBet) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new GameAlreadyFinishedException());
                    }

                    return Mono.justOrEmpty(game.getPlayerStates().stream()
                                    .filter(ps -> ps.getPlayerId() == playerId)
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new StateNotAllowedException("Player cannot double down in current state"));
                                }

                                return dealCard(game)
                                        .flatMap(card -> {
                                            playerState.getHand().add(card);
                                            playerState.setScore(CardUtils.calculateHandValue(playerState.getHand()));

                                            if (playerState.getScore() > 21) {
                                                playerState.setAction(PlayerAction.BUSTED);
                                            } else {
                                                playerState.setAction(PlayerAction.DOUBLED_DOWN);
                                            }

                                            return gameRepository.save(game);
                                        });
                            })
                            .switchIfEmpty(Mono.error(new PlayerNotFoundException()));
                });
    }

    public Mono<Game> surrender(String gameId, int playerId) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new GameAlreadyFinishedException());
                    }

                    return Mono.justOrEmpty(game.getPlayerStates().stream()
                                    .filter(ps -> ps.getPlayerId() == playerId)
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new StateNotAllowedException("Player cannot surrender in current state"));
                                }

                                playerState.setAction(PlayerAction.SURRENDERED);

                                if (game.getPlayerStates().stream()
                                        .allMatch(ps -> ps.getAction() != PlayerAction.PLAYING)) {
                                    return finishGame(game);
                                }

                                return gameRepository.save(game);
                            })
                            .switchIfEmpty(Mono.error(new PlayerNotFoundException()));
                });
    }

    public Mono<Game> finishGame(Game game) {
        System.out.println("Finishing game...");

        return Mono.fromCallable(() -> {
            while (game.getDealerScore() < 17) {
                if (game.getDeck().isEmpty()) {
                    System.out.println("Deck is empty during dealer's turn. Ending the game.");
                    game.setGameState(GameState.FINISHED);
                    return game;
                }

                String card = game.getDeck().remove(0);
                game.getDealerHand().add(card);
                game.setDealerScore(CardUtils.calculateHandValue(game.getDealerHand()));
            }

            for (PlayerState playerState : game.getPlayersState()) {
                System.out.println("Processing PlayerState: " + playerState);
                if (playerState.getAction() == PlayerAction.SURRENDERED) {
                    continue;
                }

                if (playerState.getAction() != PlayerAction.BUSTED) {
                    if (playerState.getScore() > game.getDealerScore() || game.getDealerScore() > 21) {
                        System.out.println("Player wins!");
                    } else if (playerState.getScore() == game.getDealerScore()) {
                        System.out.println("It's a tie!");
                    } else {
                        System.out.println("Dealer wins!");
                    }
                }
            }

            game.setGameState(GameState.FINISHED);
            System.out.println("Game finished: " + game);

            return game;
        }).flatMap(gameRepository::save);
    }
}
