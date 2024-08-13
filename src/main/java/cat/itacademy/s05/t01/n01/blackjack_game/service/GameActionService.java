package cat.itacademy.s05.t01.n01.blackjack_game.service;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.PlayerState;
import cat.itacademy.s05.t01.n01.blackjack_game.repository.GameRepository;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.CardUtils;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.GameState;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.PlayerAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GameActionService {

    private final GameRepository gameRepository;

    @Autowired
    public GameActionService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Mono<String> dealCard(Game game) {
        if (game.getDeck().isEmpty()) {
            return Mono.error(new IllegalStateException("No more cards in the deck"));
        }
        return Mono.just(game.getDeck().removeFirst());
    }

    public Mono<Game> hit(String gameId, int playerId) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new IllegalStateException("Game is already finished"));
                    }

                    return Mono.justOrEmpty(game.getPlayerStates().stream()
                                    .filter(ps -> ps.getPlayerId() == playerId)
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new IllegalStateException("Player cannot hit in current state"));
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
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Player not found"))); // Handle case where player is not found
                });
    }

    public Mono<Game> stand(String gameId, int playerId) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new IllegalStateException("Game is already finished"));
                    }

                    return Mono.justOrEmpty(game.getPlayerStates().stream()
                                    .filter(ps -> ps.getPlayerId() == playerId)
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new IllegalStateException("Player cannot stand in current state"));
                                }

                                playerState.setAction(PlayerAction.STANDING);

                                if (game.getPlayerStates().stream()
                                        .allMatch(ps -> ps.getAction() != PlayerAction.PLAYING)) {
                                    return finishGame(game);  // Ensure this is handled properly as a reactive operation
                                }

                                return gameRepository.save(game);
                            })
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Player not found"))); // Handle case where player is not found
                });
    }

    public Mono<Game> doubleDown(String gameId, int playerId, int amountBet) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new IllegalStateException("Game is already finished"));
                    }

                    return Mono.justOrEmpty(game.getPlayerStates().stream()
                                    .filter(ps -> ps.getPlayerId() == playerId)
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new IllegalStateException("Player cannot double down in current state"));
                                }

                                return dealCard(game)
                                        .flatMap(card -> {
                                            playerState.getHand().add(card);
                                            playerState.setScore(CardUtils.calculateHandValue(playerState.getHand()));

                                            if (playerState.getScore() > 21) {
                                                playerState.setAction(PlayerAction.BUSTED); // Player busts
                                            } else {
                                                playerState.setAction(PlayerAction.DOUBLED_DOWN);
                                                // Handle the double down bet, e.g., deduct from player's balance
                                                // Assume some logic here to handle the amountBet
                                            }

                                            return gameRepository.save(game);
                                        });
                            })
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Player not found"))); // Handle case where player is not found
                });
    }

    public Mono<Game> surrender(String gameId, int playerId) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new IllegalStateException("Game is already finished"));
                    }

                    return Mono.justOrEmpty(game.getPlayerStates().stream()
                                    .filter(ps -> ps.getPlayerId() == playerId)
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new IllegalStateException("Player cannot surrender in current state"));
                                }

                                playerState.setAction(PlayerAction.SURRENDERED);

                                if (game.getPlayerStates().stream()
                                        .allMatch(ps -> ps.getAction() != PlayerAction.PLAYING)) {
                                    return finishGame(game);  // Ensure this is handled properly as a reactive operation
                                }

                                return gameRepository.save(game);
                            })
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Player not found"))); // Handle case where player is not found
                });
    }

    public Mono<Game> finishGame(Game game) {
        return Mono.fromCallable(() -> {
            while (game.getDealerScore() < 17) {
                String card = game.getDeck().removeFirst();
                game.getDealerHand().add(card);
                game.setDealerScore(CardUtils.calculateHandValue(game.getDealerHand()));
            }

            for (PlayerState playerState : game.getPlayerStates()) {
                if (playerState.getAction() == PlayerAction.SURRENDERED) {
                    continue;
                }

                if (playerState.getAction() != PlayerAction.BUSTED) {
                    if (playerState.getScore() > game.getDealerScore() || game.getDealerScore() > 21) {
                        System.out.println("Players win!");
                    } else if (playerState.getScore() == game.getDealerScore()) {
                        System.out.println("It's a tie!");
                    } else {
                        System.out.println("Dealer wins!");
                    }
                }
            }

            game.setGameState(GameState.FINISHED);
            return game;
        }).flatMap(gameRepository::save);
    }
}
