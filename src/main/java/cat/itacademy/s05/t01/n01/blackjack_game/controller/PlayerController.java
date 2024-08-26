package cat.itacademy.s05.t01.n01.blackjack_game.controller;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Player;
import cat.itacademy.s05.t01.n01.blackjack_game.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/player")
public class PlayerController {

    @Autowired
    private GameService gameService;

    @GetMapping("/ranking")
    public Flux<Player> getPlayerRankings() {
        return gameService.getPlayerRankings();
    }

    @PutMapping("/{playerId}")
    public Mono<ResponseEntity<Player>> changePlayerName(@PathVariable String playerId, @RequestBody String newName) {
        return gameService.changePlayerName(playerId, newName)
                .map(player -> ResponseEntity.ok(player))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
