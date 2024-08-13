package cat.itacademy.s05.t01.n01.blackjack_game.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardUtils {

    public static List<String> createShuffledDeck() {
        List<String> deck = new ArrayList<>();
        String[] suits = {"H", "D", "C", "S"};
        String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

        for (String suit : suits) {
            for (String value : values) {
                deck.add(value + suit);
            }
        }

        Collections.shuffle(deck);
        return deck;
    }

    public static int calculateHandValue(List<String> hand) {
        int value = 0;
        int aces = 0;

        for (String card : hand) {
            String rank = card.substring(0, card.length() - 1);
            switch (rank) {
                case "J":
                case "Q":
                case "K":
                    value += 10;
                    break;
                case "A":
                    aces++;
                    value += 11;
                    break;
                default:
                    value += Integer.parseInt(rank);
                    break;
            }
        }
        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }
        return value;
    }
}
