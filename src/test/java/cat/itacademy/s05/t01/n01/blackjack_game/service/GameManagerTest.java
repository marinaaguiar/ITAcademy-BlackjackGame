package cat.itacademy.s05.t01.n01.blackjack_game.service;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.PlayerState;
import cat.itacademy.s05.t01.n01.blackjack_game.utils.PlayerAction;
import cat.itacademy.s05.t01.n01.blackjack_game.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class GameManagerTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameActionService gameActionService;

    @InjectMocks
    private GameManager gameManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMakeMove_Hit() {
        PlayerAction playerAction = PlayerAction.HIT;
        int amountBet = 100;

        Game mockGame = new Game();
        mockGame.setPlayerStates(List.of(new PlayerState(1)));

        when(gameRepository.findById(mockGame.getID())).thenReturn(Mono.just(mockGame));
        when(gameActionService.hit(anyString(), any(Integer.class))).thenReturn(Mono.just(mockGame));

        Mono<Game> result = gameManager.makeMove(mockGame.getID(), playerAction, amountBet);

        StepVerifier.create(result)
                .expectNext(mockGame)
                .verifyComplete();
    }
}
