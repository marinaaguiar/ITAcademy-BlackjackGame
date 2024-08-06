package cat.itacademy.s05.t01.n01.blackjack_game.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("players")
public class Player {

    @Id
    private Long id;
    private String name;
    private Integer score;
}
