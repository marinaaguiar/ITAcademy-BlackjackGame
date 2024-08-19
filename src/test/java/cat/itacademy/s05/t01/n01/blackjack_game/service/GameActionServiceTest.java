package cat.itacademy.s05.t01.n01.blackjack_game.service;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.PlayerState;
import cat.itacademy.s05.t01.n01.blackjack_game.repository.GameRepository;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.GameState;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.PlayerAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.when;

public class GameActionServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameActionService gameActionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHit() {
        String gameId = "game123";
        int playerId = 1;

        Game game = new Game();
        game.setID(gameId);
        game.setGameState(GameState.ONGOING);
        game.setDeck(new ArrayList<>(List.of("9C", "2D", "5H")));

        PlayerState playerState = new PlayerState(playerId);
        playerState.setPlayerId(playerId);
        playerState.setHand(new ArrayList<>(List.of("2H", "3D")));
        playerState.setScore(5);
        playerState.setAction(PlayerAction.PLAYING);

        game.setPlayersState(new ArrayList<>(List.of(playerState)));

        when(gameRepository.findById(gameId)).thenReturn(Mono.just(game));

        when(gameRepository.save(game)).thenReturn(Mono.just(game));

        Mono<Game> result = gameActionService.hit(gameId, playerId);

        StepVerifier.create(result)
                .expectNextMatches(updatedGame -> {
                    PlayerState updatedPlayerState = updatedGame.getPlayersState().getFirst();

                    return updatedPlayerState.getPlayerHand().contains("9C")
                            && updatedPlayerState.getScore() == 14
                            && updatedPlayerState.getAction() == PlayerAction.PLAYING;
                })
                .verifyComplete();
    }

    @Test
    public void testStand() {
        String gameId = "game123";
        int playerId = 1;

        Game game = new Game();
        game.setID(gameId);
        game.setGameState(GameState.ONGOING);

        game.setDeck(new ArrayList<>(List.of("9C", "2D", "5H")));
        game.setDealerHand(new ArrayList<>());

        PlayerState playerState = new PlayerState(playerId);
        playerState.setPlayerId(playerId);
        playerState.setHand(new ArrayList<>(List.of("2H", "3D")));
        playerState.setScore(14);
        playerState.setAction(PlayerAction.PLAYING);

        game.setPlayersState(new ArrayList<>(List.of(playerState)));

        // Mock the repository to return the game when findById is called
        when(gameRepository.findById(gameId)).thenReturn(Mono.just(game));

        // Mock the repository to save the updated game state
        when(gameRepository.save(game)).thenReturn(Mono.just(game));

        Mono<Game> result = gameActionService.stand(gameId, playerId);

        StepVerifier.create(result)
                .expectNextMatches(updatedGame -> {
                    System.out.println("Player States before stand: " + game.getPlayersState());
                    System.out.println("Player States after stand: " + updatedGame.getPlayersState());

                    PlayerState updatedPlayerState = updatedGame.getPlayersState().getFirst();

                    return updatedPlayerState.getAction() == PlayerAction.STANDING
                            && updatedPlayerState.getScore() == 14; // Score should remain unchanged
                })
                .verifyComplete();
    }

    @Test
    public void testDoubleDown_PlayerDoesNotBust() {
        String gameId = "game123";
        int playerId = 1;
        int amountBet = 50;

        Game game = new Game();
        game.setID(gameId);
        game.setGameState(GameState.ONGOING);

        PlayerState playerState = new PlayerState(playerId);
        playerState.setPlayerId(playerId);
        playerState.setHand(new ArrayList<>(List.of("5H", "6D")));
        playerState.setScore(11);
        playerState.setAction(PlayerAction.PLAYING);

        game.setPlayersState(new ArrayList<>(List.of(playerState)));
        // Player will draw a 10, resulting in a score of 21
        game.setDeck(new LinkedList<>(List.of("10S")));

        // Mock the repository to return the game when findById is called
        when(gameRepository.findById(gameId)).thenReturn(Mono.just(game));

        // Mock the repository to save the updated game state
        when(gameRepository.save(game)).thenReturn(Mono.just(game));

        Mono<Game> result = gameActionService.doubleDown(gameId, playerId, amountBet);

        StepVerifier.create(result)
                .expectNextMatches(updatedGame -> {
                    PlayerState updatedPlayerState = updatedGame.getPlayersState().get(0);

                    return updatedPlayerState.getAction() == PlayerAction.DOUBLED_DOWN
                            && updatedPlayerState.getScore() == 21
                            && updatedPlayerState.getHand().contains("10S");
                })
                .verifyComplete();
    }

    @Test
    public void testDoubleDown_PlayerBusts() {
        String gameId = "game123";
        int playerId = 1;
        int amountBet = 50;

        // Setup the initial state of the game
        Game game = new Game();
        game.setID(gameId);
        game.setGameState(GameState.ONGOING);

        PlayerState playerState = new PlayerState(playerId);
        playerState.setPlayerId(playerId);
        playerState.setHand(new ArrayList<>(List.of("10H", "7D")));
        playerState.setScore(17);
        playerState.setAction(PlayerAction.PLAYING);

        game.setPlayersState(new ArrayList<>(List.of(playerState)));
        // Player will draw a 6, resulting in a score of 23 (bust)
        game.setDeck(new LinkedList<>(List.of("6S")));

        // Mock the repository to return the game when findById is called
        when(gameRepository.findById(gameId)).thenReturn(Mono.just(game));

        // Mock the repository to save the updated game state
        when(gameRepository.save(game)).thenReturn(Mono.just(game));

        Mono<Game> result = gameActionService.doubleDown(gameId, playerId, amountBet);

        StepVerifier.create(result)
                .expectNextMatches(updatedGame -> {
                    PlayerState updatedPlayerState = updatedGame.getPlayersState().getFirst();

                    return updatedPlayerState.getAction() == PlayerAction.BUSTED
                            && updatedPlayerState.getScore() == 23 // 17 + 6 = 23
                            && updatedPlayerState.getHand().contains("6S");
                })
                .verifyComplete();
    }

    @Test
    public void testSurrender() {
        String gameId = "game123";
        int playerId = 1;

        Game game = new Game();
        game.setID(gameId);
        game.setGameState(GameState.ONGOING);

        game.setDeck(new ArrayList<>());
        game.setDealerHand(new ArrayList<>());

        PlayerState playerState1 = new PlayerState(playerId);
        playerState1.setPlayerId(playerId);
        playerState1.setHand(new ArrayList<>(List.of("5H", "6D")));
        playerState1.setScore(11);
        playerState1.setAction(PlayerAction.PLAYING);

        PlayerState playerState2 = new PlayerState(2);
        playerState2.setPlayerId(2);
        playerState2.setHand(new ArrayList<>(List.of("7H", "8D")));
        playerState2.setScore(15);
        playerState2.setAction(PlayerAction.SURRENDERED);

        game.setPlayersState(new ArrayList<>(List.of(playerState1, playerState2)));

        // Mock the repository to return the game when findById is called
        when(gameRepository.findById(gameId)).thenReturn(Mono.just(game));

        // Mock the repository to save the updated game state
        when(gameRepository.save(game)).thenReturn(Mono.just(game));

        Mono<Game> result = gameActionService.surrender(gameId, playerId);

        StepVerifier.create(result)
                .expectNextMatches(updatedGame -> {
                    PlayerState updatedPlayerState1 = updatedGame.getPlayersState().get(0);
                    PlayerState updatedPlayerState2 = updatedGame.getPlayersState().get(1);

                    return updatedPlayerState1.getAction() == PlayerAction.SURRENDERED
                            && updatedPlayerState2.getAction() == PlayerAction.SURRENDERED
                            && updatedGame.getGameState() == GameState.FINISHED;
                })
                .verifyComplete();
    }

    @Test
    public void testFinishGame_PlayerWins() {
        Game game = new Game();
        game.setID("game123");
        game.setGameState(GameState.ONGOING);

        game.setDeck(new LinkedList<>(List.of("7C"))); // Dealer will draw a 7
        game.setDealerHand(new ArrayList<>(List.of("10H"))); // Dealer starts with a 10

        PlayerState playerState = new PlayerState(1);
        playerState.setPlayerId(1);
        playerState.setHand(new ArrayList<>(List.of("9H", "10D"))); // Player score is 19
        playerState.setScore(19);
        playerState.setAction(PlayerAction.PLAYING);

        game.setPlayersState(new ArrayList<>(List.of(playerState)));

        when(gameRepository.save(game)).thenReturn(Mono.just(game));

        Mono<Game> result = gameActionService.finishGame(game);

        StepVerifier.create(result)
                .expectNextMatches(updatedGame -> {
                    return updatedGame.getDealerScore() == 17 // Dealer draws 7, total score 17
                            && updatedGame.getGameState() == GameState.FINISHED
                            && playerState.getAction() == PlayerAction.PLAYING;
                })
                .verifyComplete();
    }

    @Test
    public void testFinishGame_PlayerLoses() {
        Game game = new Game();
        game.setID("game123");
        game.setGameState(GameState.ONGOING);

        game.setDeck(new LinkedList<>(List.of("7C"))); // Dealer will draw a 7
        game.setDealerHand(new ArrayList<>(List.of("10H"))); // Dealer starts with a 10

        PlayerState playerState = new PlayerState(1);
        playerState.setPlayerId(1);
        playerState.setHand(new ArrayList<>(List.of("5H", "6D"))); // Player score is 11
        playerState.setScore(11);
        playerState.setAction(PlayerAction.PLAYING);

        game.setPlayersState(new ArrayList<>(List.of(playerState)));

        when(gameRepository.save(game)).thenReturn(Mono.just(game));

        Mono<Game> result = gameActionService.finishGame(game);

        StepVerifier.create(result)
                .expectNextMatches(updatedGame -> {
                    return updatedGame.getDealerScore() == 17
                            && updatedGame.getGameState() == GameState.FINISHED
                            && playerState.getAction() == PlayerAction.PLAYING;
                })
                .verifyComplete();
    }
}
