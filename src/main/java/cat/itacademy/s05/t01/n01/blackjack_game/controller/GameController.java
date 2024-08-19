package cat.itacademy.s05.t01.n01.blackjack_game.controller;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import cat.itacademy.s05.t01.n01.blackjack_game.model.MoveRequest;
import cat.itacademy.s05.t01.n01.blackjack_game.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import cat.itacademy.s05.t01.n01.blackjack_game.exception.GameNotFoundException;
import cat.itacademy.s05.t01.n01.blackjack_game.exception.InvalidMoveException;

import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Game> createSinglePlayerGame(@RequestBody String playerName) {
        return gameService.createSinglePlayerGame(playerName);
    }

    @PostMapping("/new/multiplayer")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Game> startNewGame(@RequestBody List<String> playerIds) {
        return gameService.startNewGame(playerIds);
    }

    @GetMapping("/{id}")
    public Mono<Game> getGameDetails(@PathVariable String id) {
        return gameService.getGameDetails(id)
                .switchIfEmpty(Mono.error(new GameNotFoundException("Game not found with id: " + id)));
    }

    @PostMapping("/{id}/play")
    public Mono<Game> makeMove(@PathVariable String id, @RequestBody MoveRequest moveRequest) {
        return gameService.makeMove(id, moveRequest.getMoveType(), moveRequest.getAmountBet())
                .onErrorMap(IllegalArgumentException.class, e -> new InvalidMoveException(e.getMessage()));
    }

    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteGame(@PathVariable String id) {
        return gameService.deleteGame(id)
                .switchIfEmpty(Mono.error(new GameNotFoundException("Game not found with id: " + id)));
    }
}
