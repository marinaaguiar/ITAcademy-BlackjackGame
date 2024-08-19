package cat.itacademy.s05.t01.n01.blackjack_game;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.MoveRequest;
import cat.itacademy.s05.t01.n01.blackjack_game.service.GameService;
import cat.itacademy.s05.t01.n01.blackjack_game.controller.GameController;

import cat.itacademy.s05.t01.n01.blackjack_game.utils.PlayerAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(GameController.class)
public class GameControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.webTestClient = WebTestClient.bindToController(gameController).build();
    }

    @Test
    public void testMakeMove() {
        String gameId = "game123";
        MoveRequest moveRequest = new MoveRequest(PlayerAction.HIT, 100);
        Game mockGame = new Game();

        when(gameService.makeMove(anyString(), any(PlayerAction.class), anyInt())).thenReturn(Mono.just(mockGame));

        webTestClient.post()
                .uri("/game/{id}/play", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(moveRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Game.class)
                .isEqualTo(mockGame);
    }
}
