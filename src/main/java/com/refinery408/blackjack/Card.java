package com.refinery408.blackjack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class Card implements Comparable<Card> {
    private Rank rank;
    private Suit suit;
    private int lowValue;
    private int highValue;

    @Override
    public String toString() {
        return this.rank.name().toLowerCase() + " of " + this.suit.name().toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return lowValue == card.lowValue &&
                highValue == card.highValue &&
                rank == card.rank &&
                suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit, lowValue, highValue);
    }

    @Override
    public int compareTo(Card card) {
        if (this.equals(card)) {
            return 0;
        } else if (this.rank == card.rank && this.suit.ordinal() < card.suit.ordinal()) {
            return -1;
        } else if (this.rank.ordinal() < card.rank.ordinal()) {
            return -1;
        } else {
            return 1;
        }
    }
}
