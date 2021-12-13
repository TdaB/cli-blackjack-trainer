package com.refinery408.blackjack;

import java.util.ArrayList;
import java.util.List;


public class Game {
    private List<Player> players = new ArrayList<>();
    private Dealer dealer;
    private Pack pack;
    private int round = 0;

    public Game() {
        this.pack = new Pack(6);
        this.pack.shuffle();
        this.dealer = new Dealer();
    }

    private void addPlayer(Player p) {
        this.players.add(p);
    }

    private void nextRound() {
        this.round++;
        System.out.println("Round " + this.round);
        System.out.println("==================================================");
        this.checkShuffle();
        this.resetPlayers();
        this.collectBets();
        this.dealCards();
        this.printGame();
        this.checkInsurance();
        if (this.isDealerBlackjack()) {
            this.nextRound();
            return;
        }
        this.checkPlayerBlackjack();
        this.players
                .stream()
                .filter(Player::isActive)
                .forEach(p -> {
                    this.takePlayerTurn(p);
                    System.out.println("--------------------------------------------------");
                });
        if (this.areAllPlayersInactive()) {
            this.nextRound();
            return;
        }
        this.resolveDoubleDowns();
        this.playDealer();
        this.nextRound();
    }

    private void checkShuffle() {
        if (this.pack.getPackIndex() >= this.pack.getShuffleIndex()) {
            System.out.println("Shuffling pack...");
            this.pack.shuffle();
        }
    }

    private void resetPlayers() {
        this.dealer.setHand(new Hand());
        for (Player p : this.players) {
            p.setHands(new ArrayList<>());
            p.addHand(new Hand());
        }
    }

    private void collectBets() {
        for (Player p : this.players) {
            p.collectBet();
        }
    }

    private void dealCards() {
        for (Player p : this.players) {
            p.getHand(0).addCard(this.pack.drawCard());
        }
        this.dealer.getHand().addCard(this.pack.drawCard());
        for (Player p : this.players) {
            p.getHand(0).addCard(this.pack.drawCard());
        }
        this.dealer.getHand().addCard(this.pack.drawCard());
    }

    private void checkInsurance() {
        if (this.dealer.getHand().getRevealedDealerCard().getRank() != Rank.ACE) {
            return;
        }
        for (Player p : this.players) {
            p.collectInsurance();
        }
        if (this.dealer.getHand().isBlackjack()) {
            System.out.println("Dealer fucking had it--unbelievable!");
            for (Player p : this.players) {
                if (p.getInsurance() > 0) {
                    this.payPlayer(p, p.getInsurance() * 2);
                    p.setInsurance(0);
                }
            }
        }
    }

    private boolean isDealerBlackjack() {
        if (this.dealer.getHand().isBlackjack()) {
            System.out.println("Dealer got dat blackjack!");
            this.dealer.printDealerFullHand();
            for (Player p : this.players) {
                Hand h = p.getHand(0);
                if (h.isBlackjack()) {
                    this.payPlayer(p, h.getCurrentBet());
                }
            }
            return true;
        }
        return false;
    }

    private void checkPlayerBlackjack() {
        for (Player p : this.players) {
            Hand h = p.getHand(0);
            if (h.isBlackjack()) {
                System.out.println(p.getName() + " has blackjack baby!");
                this.payPlayer(p, (int) (h.getCurrentBet() * 2.5));
                h.setActive(false);
            }
        }
    }

    private void takePlayerTurn(Player p) {
        System.out.println(p.getName() + "'s turn");
        if (p.canDoubleDown()) {
            if (this.doubleDown(p)) {
                return;
            }
        }
        this.playHand(p, p.getHand(0));
    }

    private void playHand(Player p, Hand h) {
        h.printHand();
        if (h.isBust()) {
            System.out.println(String.format("%s busted with %d!", p.getName(), h.lowValue()));
            h.setActive(false);
            return;
        }
        if (h.is21()) {
            return;
        }
        if (h.size() == 1 && h.getCard(0).getRank() == Rank.ACE) {
            this.handleSplitAce(p, h);
            return;
        }
        if (h.canSplit() && p.getMoney() >= h.getCurrentBet()) {
            if (this.split(p, h)) {
                return;
            }
        }
        boolean hit = p.getDecisionHitOrStand();
        if (hit) {
            h.addCard(this.pack.drawCard());
            this.playHand(p, h);
        }
    }

    private void handleSplitAce(Player p, Hand h) {
        h.addCard(this.pack.drawCard());
        h.printHand();
        if (h.isBlackjack()) {
            System.out.println("Mini blackjack from split ace!");
            this.payPlayer(p, h.getCurrentBet() * 2);
        }
    }

    private boolean split(Player p, Hand h) {
        System.out.println("Would you like to split?");
        boolean yes = p.getDecisionYesOrNo();
        if (!yes) {
            return false;
        }
        try {
            p.deductMoney(h.getCurrentBet());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }

        Hand newHand = new Hand();
        newHand.addCard(h.removeCard(0));
        newHand.setCurrentBet(h.getCurrentBet());
        p.addHand(newHand);

        this.playHand(p, h);
        this.playHand(p, newHand);

        return true;
    }

    private boolean doubleDown(Player p) {
        p.getHand(0).printHand();
        System.out.println("Would you like to double down, " + p.getName() + "?");
        boolean yes = p.getDecisionYesOrNo();
        if (!yes) {
            return false;
        }
        try {
            Hand h = p.getHand(0);
            p.deductMoney(h.getCurrentBet());
            h.setCurrentBet(h.getCurrentBet() * 2);
            h.setDoubleDownCard(this.pack.drawCard());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return true;
    }

    private void resolveDoubleDowns() {
        for (Player p : this.players) {
            Card ddc = p.getHand(0).getDoubleDownCard();
            if (ddc != null) {
                System.out.println("Resolving double down...");
                p.getHand(0).addCard(ddc);
                p.getHand(0).setDoubleDownCard(null);
                p.printPlayer();
            }
        }
    }

    private void playDealer() {
        this.dealer.printDealerFullHand();
        int high = this.dealer.getHand().highValue();
        if (high < 17) {
            System.out.println("Dealer hits...");
            this.dealer.getHand().addCard(this.pack.drawCard());
            this.playDealer();
            return;
        }
        if (high > 21) {
            System.out.println("Dealer busted!");
            this.dealer.printDealerFullHand();
            this.players
                    .stream()
                    .filter(Player::isActive)
                    .forEach(p -> p
                            .getHands()
                            .stream()
                            .filter(h -> !h.isBlackjack() && !h.isBust())
                            .forEach(h -> this.payPlayer(p, h.getCurrentBet() * 2)));
        } else {
            this.compareDealerWithLivePlayers(high);
        }
    }

    private void compareDealerWithLivePlayers(int dealerHigh) {
        this.players
                .stream()
                .filter(Player::isActive)
                .forEach(p -> {
                    p.getHands().stream()
                     .filter(Hand::isActive)
                     .forEach(h -> {
                         int pHigh = h.highValue();
                         if (pHigh > dealerHigh) {
                             System.out.println(String.format("%s beat the dealer! (%d vs. %d)\n", p.getName(), pHigh, dealerHigh));
                             this.payPlayer(p, h.getCurrentBet() * 2);
                         } else if (pHigh == dealerHigh) {
                             System.out.println(String.format("%s tied the dealer. (%d vs. %d)\n", p.getName(), pHigh, dealerHigh));
                             this.payPlayer(p, h.getCurrentBet());
                         } else {
                             System.out.println(String.format("%s lost to the dealer. (%d vs. %d)\n", p.getName(), pHigh, dealerHigh));
                         }
                         h.printHand();
                         this.dealer.printDealerFullHand();
                     });
                });
    }

    private boolean areAllPlayersInactive() {
        for (Player p : this.players) {
            if (p.isActive()) {
                return false;
            }
        }
        return true;
    }

    private void payPlayer(Player p, int amount) {
        p.addMoney(amount);
        System.out.println(String.format("%s made $%d!", p.getName(), amount));
    }

    private void printGame() {
        this.dealer.printDealer();
        for (Player p : this.players) {
            p.printPlayer();
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        Player tom = new Player("Player 1", 69);
        Player sod = new Player("Some other dude", 69);
        game.addPlayer(tom);
        game.addPlayer(sod);
        game.nextRound();
    }
}
