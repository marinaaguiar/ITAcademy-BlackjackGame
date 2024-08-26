package cat.itacademy.s05.t01.n01.blackjack_game.controller;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Player;
import cat.itacademy.s05.t01.n01.blackjack_game.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest(PlayerController.class)
public class PlayerControllerTest {

    @MockBean
    private GameService gameService;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetPlayerRankings() {
        Player player1 = new Player("Player1", 100);
        Player player2 = new Player("Player2", 150);
        Player player3 = new Player("Player3", 120);

        when(gameService.getPlayerRankings()).thenReturn(Flux.just(player2, player3, player1));

        webTestClient.get().uri("/player/ranking")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Player.class)
                .contains(player2, player3, player1);
    }

    @Test
    public void testChangePlayerName() {
        String playerId = "1";
        String newName = "New Name";
        Player updatedPlayer = new Player(playerId, 100);
        updatedPlayer.setName(newName);

        when(gameService.changePlayerName(playerId, newName)).thenReturn(Mono.just(updatedPlayer));

        webTestClient.put().uri("/player/" + playerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newName)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Player.class)
                .isEqualTo(updatedPlayer);
    }

    @Test
    public void testChangePlayerName_PlayerNotFound() {
        String playerId = "0";
        String newName = "New Name";

        when(gameService.changePlayerName(playerId, newName)).thenReturn(Mono.empty());

        webTestClient.put().uri("/player/" + playerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newName)
                .exchange()
                .expectStatus().isNotFound();
    }
}
