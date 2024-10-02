package cat.itacademy.s05.t01.n01.blackjack_game.service;

import cat.itacademy.s05.t01.n01.blackjack_game.exception.GameNotFoundException;
import cat.itacademy.s05.t01.n01.blackjack_game.exception.PlayerNotFoundException;
import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.GameState;
import cat.itacademy.s05.t01.n01.blackjack_game.model.Player;
import cat.itacademy.s05.t01.n01.blackjack_game.model.PlayerState;
import cat.itacademy.s05.t01.n01.blackjack_game.repository.GameRepository;
import cat.itacademy.s05.t01.n01.blackjack_game.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class GameManagerTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameActionInteractor gameActionInteractor;

    @InjectMocks
    private GameManager gameManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testCreateSinglePlayerGame() {
        String playerName = "Player1";
        Player player = new Player(playerName, 0);
        player.setId("1");

        when(playerRepository.save(any(Player.class))).thenReturn(Mono.just(player));

        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            game.setId(UUID.randomUUID().toString());
            return Mono.just(game);
        });

        Mono<Game> result = gameManager.createSinglePlayerGame(playerName);

        StepVerifier.create(result)
                .expectNextMatches(game -> {
                    return game.getGameState() == GameState.ONGOING
                            && game.getPlayersState().size() == 1
                            && game.getPlayersState().get(0).getPlayerId() == player.getId()
                            && game.getPlayersState().get(0).getPlayerHand().isEmpty()
                            && game.getPlayersState().get(0).getScore() == 0
                            && game.getDealerHand().isEmpty()
                            && game.getDealerScore() == 0;
                })
                .verifyComplete();

        verify(gameActionInteractor).initializeGame(any(Game.class));
        verify(playerRepository).save(any(Player.class));
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    public void testStartNewGame_Success() {
        List<String> playerIds = List.of("1", "2");
        Player player1 = new Player("Player1", 0);
        player1.setId("1");
        Player player2 = new Player("Player2", 0);
        player2.setId("2");

        when(playerRepository.findAllById(playerIds)).thenReturn(Flux.just(player1, player2));

        doNothing().when(gameActionInteractor).initializeGame(any(Game.class));

        when(gameActionInteractor.dealCard(any(Game.class)))
                .thenAnswer(invocation -> {
                    Game game = invocation.getArgument(0);
                    List<PlayerState> playerStates = game.getPlayersState();

                    if (playerStates.get(0).getPlayerHand().isEmpty()) {
                        return Mono.just("5H"); // First card to player 1
                    } else if (playerStates.get(0).getPlayerHand().size() == 1) {
                        return Mono.just("6D"); // Second card to player 1
                    } else if (playerStates.get(1).getPlayerHand().isEmpty()) {
                        return Mono.just("7S"); // First card to player 2
                    } else {
                        return Mono.just("8C"); // Second card to player 2
                    }
                });

        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            game.setId(UUID.randomUUID().toString()); // Simulate game ID assignment
            return Mono.just(game);
        });

        Mono<Game> result = gameManager.startNewGame(playerIds);

        StepVerifier.create(result)
                .expectNextMatches(game -> {
                    List<PlayerState> playerStates = game.getPlayersState();
                    boolean player1HasTwoCards = playerStates.get(0).getPlayerHand().size() == 2;
                    boolean player2HasTwoCards = playerStates.get(1).getPlayerHand().size() == 2;

                    boolean correctScores = playerStates.get(0).getScore() == 11 &&  // 5H + 6D
                            playerStates.get(1).getScore() == 15;  // 7S + 8C

                    return game.getGameState() == GameState.ONGOING
                            && playerStates.size() == 2
                            && player1HasTwoCards
                            && player2HasTwoCards
                            && correctScores;
                })
                .verifyComplete();

        verify(playerRepository).findAllById(playerIds);
        verify(gameActionInteractor).initializeGame(any(Game.class));
        verify(gameActionInteractor, times(4)).dealCard(any(Game.class)); // 2 players, 2 cards each
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    public void testStartNewGame_PlayerNotFound() {
        List<String> playerIds = List.of("1", "2");
        Player player1 = new Player("Player1", 0);
        player1.setId("1");

        when(playerRepository.findAllById(playerIds)).thenReturn(Flux.just(player1)); // Only one player found

        Mono<Game> result = gameManager.startNewGame(playerIds);

        StepVerifier.create(result)
                .expectError(PlayerNotFoundException.class)
                .verify();

        verify(playerRepository).findAllById(playerIds);
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    public void testGetPlayerRankings() {
        Player player1 = new Player("Player1", 100);
        Player player2 = new Player("Player2", 150);
        Player player3 = new Player("Player3", 120);

        when(playerRepository.findAll()).thenReturn(Flux.just(player1, player2, player3));

        StepVerifier.create(gameManager.getPlayerRankings())
                .expectNext(player2) // Player2 has the highest score
                .expectNext(player3) // Player3 has the second highest score
                .expectNext(player1) // Player1 has the lowest score
                .verifyComplete();
    }

    @Test
    public void testChangePlayerName_Success() {
        String playerId = "1";
        String newName = "Player_1 Updated";

        Player player = new Player(playerId, 100);

        when(playerRepository.findById(playerId)).thenReturn(Mono.just(player));
        when(playerRepository.save(player)).thenReturn(Mono.just(player));

        Mono<Player> result = gameManager.changePlayerName(playerId, newName);

        StepVerifier.create(result)
                .expectNextMatches(updatedPlayer -> updatedPlayer.getName().equals(newName))
                .verifyComplete();
    }

    @Test
    public void testChangePlayerName_PlayerNotFound() {
        String playerId = "1";
        String newName = "Player_1 Updated";

        when(playerRepository.findById(playerId)).thenReturn(Mono.empty());

        Mono<Player> result = gameManager.changePlayerName(playerId, newName);

        StepVerifier.create(result)
                .expectError(PlayerNotFoundException.class)
                .verify();
    }

    @Test
    public void testGetGameDetails_Success() {
        String gameId = "game123";

        Game game = new Game();
        game.setId(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Mono.just(game));

        Mono<Game> result = gameManager.getGameDetails(gameId);

        StepVerifier.create(result)
                .expectNext(game)
                .verifyComplete();
    }

    @Test
    public void testGetGameDetails_GameNotFound() {
        String gameId = "game123";

        when(gameRepository.findById(gameId)).thenReturn(Mono.empty());

        Mono<Game> result = gameManager.getGameDetails(gameId);

        StepVerifier.create(result)
                .expectError(GameNotFoundException.class)
                .verify();
    }

    @Test
    public void testDeleteGame_Success() {
        String gameId = "game123";
        Game game = new Game();
        game.setId(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Mono.just(game));
        when(gameRepository.delete(game)).thenReturn(Mono.empty());

        Mono<Void> result = gameRepository.findById(gameId)
                .flatMap(foundGame -> gameRepository.delete(foundGame));

        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(gameRepository).delete(game);
    }

    @Test
    public void testDeleteGame_GameNotFound() {
        String gameId = "game123";

        when(gameRepository.findById(gameId)).thenReturn(Mono.empty());

        StepVerifier.create(gameManager.deleteGame(gameId))
                .expectError(GameNotFoundException.class)
                .verify();

        verify(gameRepository, never()).delete(any(Game.class));
    }
}
