package cat.itacademy.s05.t01.n01.blackjack_game.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "games")
public class Game {

    @Id
    private String id;
    private List<Player> players;
    private String status;
}
