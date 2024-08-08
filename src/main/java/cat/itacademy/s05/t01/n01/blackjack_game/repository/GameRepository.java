package cat.itacademy.s05.t01.n01.blackjack_game.repository;

import cat.itacademy.s05.t01.n01.blackjack_game.model.Game;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface GameRepository extends ReactiveMongoRepository<Game, String> {
}
