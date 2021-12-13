package com.refinery408.blackjack;

public class Dealer {
    private Hand hand;

    public Dealer() {
    }

    protected Hand getHand() {
        return hand;
    }

    protected void setHand(Hand hand) {
        this.hand = hand;
    }

    protected void printDealer() {
        System.out.println("Dealer");
        this.getHand().printDealerRevealedCard();
        System.out.println();
    }

    protected void printDealerFullHand() {
        System.out.println("Dealer");
        this.getHand().printHand();
    }
}
