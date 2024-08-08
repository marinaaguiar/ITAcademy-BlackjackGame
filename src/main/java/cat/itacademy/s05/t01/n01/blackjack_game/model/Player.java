package cat.itacademy.s05.t01.n01.blackjack_game.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("players")
public class Player {

    @Id
    private String id;
    private String name;
    private int score;
    private int totalWins;
    private int totalLosses;

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public String getId() {
        return id;
    }
}
