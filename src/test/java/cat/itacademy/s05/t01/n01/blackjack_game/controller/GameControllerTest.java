package cat.itacademy.s05.t01.n01.blackjack_game.controller;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.MoveRequest;
import cat.itacademy.s05.t01.n01.blackjack_game.service.GameService;

import cat.itacademy.s05.t01.n01.blackjack_game.model.GameState;
import cat.itacademy.s05.t01.n01.blackjack_game.model.PlayerAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(GameController.class)
public class GameControllerTest {

    @MockBean
    private GameService gameService;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateSinglePlayerGame() {
        String playerName = "Player1";
        Game game = new Game();

        when(gameService.createSinglePlayerGame(anyString())).thenReturn(Mono.just(game));

        webTestClient.post().uri("/game/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("\"Player1\"")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Game.class)
                .consumeWith(response -> {
                    Game responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals(game.getId(), responseBody.getId());
                    assertEquals(game.getGameState(), responseBody.getGameState());
                    assertEquals(game.getPlayersState(), responseBody.getPlayersState());
                    assertEquals(game.getDeck(), responseBody.getDeck());
                    assertEquals(game.getDealerHand(), responseBody.getDealerHand());
                    assertEquals(game.getDealerScore(), responseBody.getDealerScore());
                });
    }

    @Test
    public void testStartNewGame() {
        List<String> playerIds = List.of("1", "2");
        Game game = new Game();

        when(gameService.startNewGame(playerIds)).thenReturn(Mono.just(game));

        webTestClient.post().uri("/game/new/multiplayer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(playerIds)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Game.class)
                .consumeWith(response -> {
                    Game responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals(game.getId(), responseBody.getId());
                    assertEquals(game.getGameState(), responseBody.getGameState());
                    assertEquals(game.getPlayersState(), responseBody.getPlayersState());
                    assertEquals(game.getDeck(), responseBody.getDeck());
                    assertEquals(game.getDealerHand(), responseBody.getDealerHand());
                    assertEquals(game.getDealerScore(), responseBody.getDealerScore());
                });
    }

    @Test
    public void testGetGameDetails() {
        String gameId = "game123";
        Game game = new Game();

        when(gameService.getGameDetails(gameId)).thenReturn(Mono.just(game));

        webTestClient.get().uri("/game/" + gameId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Game.class)
                .consumeWith(response -> {
                    Game responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals(game.getId(), responseBody.getId());
                    assertEquals(game.getGameState(), responseBody.getGameState());
                    assertEquals(game.getPlayersState(), responseBody.getPlayersState());
                    assertEquals(game.getDeck(), responseBody.getDeck());
                    assertEquals(game.getDealerHand(), responseBody.getDealerHand());
                    assertEquals(game.getDealerScore(), responseBody.getDealerScore());
                });
    }

    @Test
    public void testDeleteGame() {
        String gameId = "game123";

        when(gameService.deleteGame(gameId)).thenReturn(Mono.empty());

        webTestClient.delete().uri("/game/" + gameId + "/delete")
                .exchange()
                .expectStatus().isNoContent(); // Expect 204 NO_CONTENT
    }

    @Test
    public void testMakeMove() {
        String gameId = "game123";
        MoveRequest moveRequest = new MoveRequest(PlayerAction.HIT, 100);

        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setGameState(GameState.ONGOING);
        mockGame.setDealerScore(10);
        mockGame.setDealerHand(List.of("2H", "3D"));

        when(gameService.makeMove(anyString(), anyString(), any(PlayerAction.class), anyInt())).thenReturn(Mono.just(mockGame));

        webTestClient.post()
                .uri("/game/{id}/play", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(moveRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Game.class)
                .consumeWith(response -> {
                    Game responseBody = response.getResponseBody();
                    assertNotNull(responseBody);
                    assertEquals(mockGame.getId(), responseBody.getId());
                    assertEquals(mockGame.getGameState(), responseBody.getGameState());
                    assertEquals(mockGame.getDealerScore(), responseBody.getDealerScore());
                    assertEquals(mockGame.getDealerHand(), responseBody.getDealerHand());
                });
    }
}
