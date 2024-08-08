package cat.itacademy.s05.t01.n01.blackjack_game.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardUtils {

    private static final List<String> DECK = List.of(
            // Hearts
            "2H", "3H", "4H", "5H", "6H", "7H", "8H", "9H", "10H", "JH", "QH", "KH", "AH",
            // Diamonds
            "2D", "3D", "4D", "5D", "6D", "7D", "8D", "9D", "10D", "JD", "QD", "KD", "AD",
            // Clubs
            "2C", "3C", "4C", "5C", "6C", "7C", "8C", "9C", "10C", "JC", "QC", "KC", "AC",
            // Spades
            "2S", "3S", "4S", "5S", "6S", "7S", "8S", "9S", "10S", "JS", "QS", "KS", "AS"
    );

    public static List<String> createShuffledDeck() {
        List<String> deck = new ArrayList<>(DECK);
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
