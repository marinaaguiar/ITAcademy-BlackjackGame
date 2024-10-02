package cat.itacademy.s05.t01.n01.blackjack_game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"cat.itacademy.s05.t01.n01.blackjack_game", "package.where.swaggerconfig.is.located"})

public class BlackjackGameApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlackjackGameApplication.class, args);
	}

}
