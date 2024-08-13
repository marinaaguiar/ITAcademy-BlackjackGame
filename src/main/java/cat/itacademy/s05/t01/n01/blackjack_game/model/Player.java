package cat.itacademy.s05.t01.n01.blackjack_game.model;

import jakarta.persistence.Entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.util.UUID;

@Entity
@Table("players")
public class Player {

    @Id
    private int id;
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

    public int getId() {
        return id;
    }
}
