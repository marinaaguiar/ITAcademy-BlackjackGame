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

    public Mono<Game> hit(String gameId, String playerId) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new GameAlreadyFinishedException());
                    }

                    return Mono.justOrEmpty(game.getPlayersState().stream()
                                    .filter(ps -> ps.getPlayerId().equals(playerId))
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new StateNotAllowedException("Player cannot hit in current state"));
                                }

                                return dealCard(game)
                                        .flatMap(card -> {
                                            playerState.getPlayerHand().add(card);
                                            playerState.setScore(CardUtils.calculateHandValue(playerState.getPlayerHand()));
                                            System.out.println("Player " + playerId + " hit and received: " + card + ". New score: " + playerState.getScore());

                                            if (playerState.getScore() > 21) {
                                                playerState.setAction(PlayerAction.BUSTED);
                                                System.out.println("Player " + playerId + " has busted.");
                                                // Check if all players are busted after this hit
                                                return finishGame(game);
                                            }

                                            return gameRepository.save(game);
                                        });
                            })
                            .switchIfEmpty(Mono.error(new PlayerNotFoundException()));
                });
    }

    public Mono<Game> stand(String gameId, String playerId) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new GameAlreadyFinishedException());
                    }

                    return Mono.justOrEmpty(game.getPlayersState().stream()
                                    .filter(ps -> ps.getPlayerId().equals(playerId))
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new StateNotAllowedException("Player cannot stand in current state"));
                                }

                                playerState.setAction(PlayerAction.STANDING);
                                System.out.println("Player " + playerId + " has stood.");

                                // Check if all players have finished their actions
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

    public Mono<Game> doubleDown(String gameId, String playerId, int amountBet) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new GameAlreadyFinishedException());
                    }

                    return Mono.justOrEmpty(game.getPlayersState().stream()
                                    .filter(ps -> ps.getPlayerId().equals(playerId))
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new StateNotAllowedException("Player cannot double down in current state"));
                                }

                                return dealCard(game)
                                        .flatMap(card -> {
                                            playerState.getPlayerHand().add(card);
                                            playerState.setScore(CardUtils.calculateHandValue(playerState.getPlayerHand()));

                                            System.out.println("Player " + playerId + " doubled down and received: " + card + ". New score: " + playerState.getScore());

                                            if (playerState.getScore() > 21) {
                                                playerState.setAction(PlayerAction.BUSTED);
                                                System.out.println("Player " + playerId + " has busted.");
                                                // Check if all players are busted after this action
                                                return finishGame(game);
                                            } else {
                                                playerState.setAction(PlayerAction.DOUBLED_DOWN);
                                            }

                                            return gameRepository.save(game);
                                        });
                            })
                            .switchIfEmpty(Mono.error(new PlayerNotFoundException()));
                });
    }

    public Mono<Game> surrender(String gameId, String playerId) {
        return gameRepository.findById(gameId)
                .flatMap(game -> {
                    if (game.getGameState() == GameState.FINISHED) {
                        return Mono.error(new GameAlreadyFinishedException());
                    }

                    return Mono.justOrEmpty(game.getPlayersState().stream()
                                    .filter(ps -> ps.getPlayerId().equals(playerId))
                                    .findFirst())
                            .flatMap(playerState -> {
                                if (playerState.getAction() != PlayerAction.PLAYING) {
                                    return Mono.error(new StateNotAllowedException("Player cannot surrender in current state"));
                                }

                                playerState.setAction(PlayerAction.SURRENDERED);
                                System.out.println("Player " + playerId + " has surrendered.");

                                // Check if all players have finished their actions
                                if (game.getPlayersState().stream()
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

        // Check if all players are busted
        boolean allPlayersBusted = game.getPlayersState().stream()
                .allMatch(playerState -> playerState.getAction() == PlayerAction.BUSTED);

        if (allPlayersBusted) {
            System.out.println("All players are busted. The dealer wins!");
            game.setGameState(GameState.FINISHED);
            return gameRepository.save(game);
        }

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
                System.out.println("Dealer drew a card: " + card + ", new score: " + game.getDealerScore());
            }

            for (PlayerState playerState : game.getPlayersState()) {
                System.out.println("Processing PlayerState: " + playerState);
                if (playerState.getAction() == PlayerAction.SURRENDERED) {
                    continue;
                }

                if (playerState.getAction() != PlayerAction.BUSTED) {
                    if (playerState.getScore() > game.getDealerScore() || game.getDealerScore() > 21) {
                        System.out.println("Player " + playerState.getPlayerId() + " wins!");
                    } else if (playerState.getScore() == game.getDealerScore()) {
                        System.out.println("Player " + playerState.getPlayerId() + " ties with the dealer!");
                    } else {
                        System.out.println("Dealer wins against player " + playerState.getPlayerId() + "!");
                    }
                }
            }

            game.setGameState(GameState.FINISHED);
            System.out.println("Game finished: " + game);

            return game;
        }).flatMap(gameRepository::save);
    }
}
