package com.refinery408.blackjack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Hand {
    protected List<Card> cards = new ArrayList<>();
    protected Card doubleDownCard;
    protected int currentBet;
    protected boolean isActive = true;

    protected Card getDoubleDownCard() {
        return this.doubleDownCard;
    }

    protected void setDoubleDownCard(Card doubleDownCard) {
        this.doubleDownCard = doubleDownCard;
    }

    protected int getCurrentBet() {
        return currentBet;
    }

    protected void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }

    protected boolean isActive() {
        return isActive;
    }

    protected void setActive(boolean active) {
        isActive = active;
    }

    protected void addCard(Card c) {
        this.cards.add(c);
    }

    protected Card getCard(int i) {
        return this.cards.get(i);
    }

    protected int lowValue() {
        return this.cards.stream().mapToInt(Card::getLowValue).sum();
    }

    protected int highValue() {
        return highValueHelper(0, 0);
    }

    protected int highValueHelper(int total, int index) {
        if (index == this.cards.size()) {
            return total;
        }
        Card card = this.cards.get(index);
        int low = card.getLowValue();
        int high = card.getHighValue();
        if (low != high) {
            return this.bestValue(
                    highValueHelper(total + low, index + 1),
                    highValueHelper(total + high, index + 1));
        }
        return this.highValueHelper(total + low, index + 1);
    }

    protected int bestValue(int left, int right) {
        if (left > 21 && right > 21) {
            return Math.min(left, right);
        }
        if (left > 21) {
            return right;
        }
        if (right > 21) {
            return left;
        }
        return Math.max(left, right);
    }

    protected boolean isBust() {
        return this.lowValue() > 21;
    }

    protected boolean isBlackjack() {
        if (this.cards.size() != 2) {
            return false;
        }
        Card left = this.cards.get(0);
        Card right = this.cards.get(1);
        return (left.getRank() == Rank.ACE && right.getLowValue() == 10) ||
                (right.getRank() == Rank.ACE && left.getLowValue() == 10);
    }

    protected boolean is21() {
        return this.highValue() == 21 || this.lowValue() == 21;
    }

    protected void printHand() {
        if (this.getCurrentBet() > 0) {
            System.out.println("Current bet: " + this.getCurrentBet());
        }
        for (Card c : this.cards) {
            System.out.println(c);
        }
        int low = this.lowValue();
        int high = this.highValue();
        if (low == high) {
            System.out.println("Value: " + low);
        } else {
            System.out.println("Low value: " + low);
            System.out.println("High value: " + high);
        }
        System.out.println();
    }

    protected Card getRevealedDealerCard() {
        if (this.cards.size() > 1) {
            return this.cards.get(0);
        }
        return null;
    }

    protected void printDealerRevealedCard() {
        if (this.cards.size() >= 1) {
            System.out.println(this.cards.get(0));
        }
    }

    protected boolean canDoubleDown() {
        int low = this.lowValue();
        int high = this.highValue();
        return low == 9 || low == 10 || low == 11 || high == 9 || high == 10 || high == 11;
    }

    protected boolean canSplit() {
        if (this.cards.size() != 2) {
            return false;
        }
        return this.cards.get(0).getRank() == this.cards.get(1).getRank();
    }

    protected Card removeCard(int index) {
        return this.cards.remove(index);
    }

    protected boolean equals(Hand h) {
        return this.cards.stream().sorted().collect(Collectors.toList()).equals(
                h.cards.stream().sorted().collect(Collectors.toList()));
    }

    protected int size() {
        return this.cards.size();
    }
}
