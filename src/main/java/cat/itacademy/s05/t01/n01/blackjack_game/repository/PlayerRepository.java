package cat.itacademy.s05.t01.n01.blackjack_game.repository;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Player;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PlayerRepository extends ReactiveCrudRepository<Player, Long> {
}
