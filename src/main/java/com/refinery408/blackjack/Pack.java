package com.refinery408.blackjack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class Pack {
    private List<Card> cards = new ArrayList<>();
    private List<Card> graveyard = new ArrayList<>();
    private int packIndex;
    private int shuffleIndex;

    public Pack(int decks) {
        List<Rank> numberRanks =
                Arrays.asList(Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE);
        List<Rank> tenRanks =
                Arrays.asList(Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING);
        for (int i = 0; i < decks; i++) {
            for (Suit suit : Suit.values()) {
                this.cards.add(new Card(Rank.ACE, suit, 1, 11));
                int value = 2;
                for (Rank rank : numberRanks) {
                    this.cards.add(new Card(rank, suit, value, value));
                    value++;
                }
                for (Rank rank : tenRanks) {
                    this.cards.add(new Card(rank, suit, 10, 10));
                }
            }
        }
        this.packIndex = 0;
        this.shuffleIndex = (int) (this.cards.size() * .75);
    }

    protected int getPackIndex() {
        return packIndex;
    }

    protected void setPackIndex(int packIndex) {
        this.packIndex = packIndex;
    }

    protected int getShuffleIndex() {
        return shuffleIndex;
    }

    protected void setShuffleIndex(int shuffleIndex) {
        this.shuffleIndex = shuffleIndex;
    }

    protected void shuffle() {
        this.cards.addAll(this.graveyard);
        this.graveyard = new ArrayList<>();
        this.packIndex = 0;

        int size = this.cards.size();
        Random random = new Random();
        for (int index = 0; index < size; index++) {
            int randomIndex = random.nextInt(size);
            if (index == randomIndex) {
                continue;
            }
            Card temp = this.cards.get(index);
            Card toSwap = this.cards.get(randomIndex);
            this.cards.set(index, toSwap);
            this.cards.set(randomIndex, temp);
        }
    }

    protected void printPack() {
        for (Card c : this.cards) {
            System.out.println(c);
        }
    }

    protected void printGraveyard() {
        for (Card c : this.graveyard) {
            System.out.println(c);
        }
    }

    protected Card drawCard() {
        Card c = this.cards.remove(0);
        this.graveyard.add(c);
        this.packIndex++;
        return c;
    }
}
