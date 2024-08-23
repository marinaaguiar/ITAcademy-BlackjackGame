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
    public void testMakeMove() {
        String gameId = "game123";
        MoveRequest moveRequest = new MoveRequest(PlayerAction.HIT, 100);

        Game mockGame = new Game();
        mockGame.setID(gameId);
        mockGame.setGameState(GameState.ONGOING);
        mockGame.setDealerScore(10);
        mockGame.setDealerHand(List.of("2H", "3D"));

        when(gameService.makeMove(anyString(), any(PlayerAction.class), anyInt())).thenReturn(Mono.just(mockGame));

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
                    assertEquals(mockGame.getID(), responseBody.getID());
                    assertEquals(mockGame.getGameState(), responseBody.getGameState());
                    assertEquals(mockGame.getDealerScore(), responseBody.getDealerScore());
                    assertEquals(mockGame.getDealerHand(), responseBody.getDealerHand());
                });
    }
}
