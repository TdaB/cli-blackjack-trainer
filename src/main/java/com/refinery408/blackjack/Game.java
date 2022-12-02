package com.refinery408.blackjack;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class Game {
    private Logger log = LoggerFactory.getLogger(Game.class);
    private List<Player> players = new ArrayList<>();
    private Dealer dealer;
    private Pack pack;
    private int round = 0;
    private boolean enableTrainer;

    public Game(boolean enableTrainer) {
        this.enableTrainer = enableTrainer;
        this.pack = new Pack(6);
        this.pack.shuffle();
        this.dealer = new Dealer();
    }

    private void addPlayer(Player p) {
        this.players.add(p);
    }

    private void nextRound() {
        this.round++;
        log.info("==================================================");
        log.info("Round " + this.round);
        log.info("==================================================");
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
                });
        if (this.areAllPlayersInactive()) {
            this.nextRound();
            return;
        }
        this.resolveDoubleDowns();
        log.info("--------------------------------------------------");
        log.info("Dealer's turn");
        log.info("--------------------------------------------------");
        this.playDealer();
        this.nextRound();
    }

    private void checkShuffle() {
        if (this.pack.getPackIndex() >= this.pack.getShuffleIndex()) {
            log.info("Shuffling pack...");
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
            log.info("");
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
            log.info("Dealer fucking had it--unbelievable!");
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
            log.info("Dealer got dat blackjack!");
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
                log.info(p.getName() + " has blackjack baby!");
                this.payPlayer(p, (int) (h.getCurrentBet() * 2.5));
                h.setActive(false);
            }
        }
    }

    private void takePlayerTurn(Player p) {
        log.info("--------------------------------------------------");
        log.info(p.getName() + "'s turn");
        log.info("--------------------------------------------------");
        if (p.canDoubleDown()) {
            if (this.doubleDown(p)) {
                return;
            }
        }
        this.playHand(p, p.getHand(0));
    }

    private void playHand(Player p, Hand h) {
        h.printHand();
        this.dealer.printDealer();

        if (h.isBust()) {
            log.info("{} busted with {}!\n", p.getName(), h.lowValue());
            h.setActive(false);
            return;
        }
        if (h.is21()) {
            return;
        }

        if (h.isSingletonAce()) {
            this.handleSplitAce(p, h);
            return;
        }
        if (h.canSplit(p.getMoney())) {
            if (this.split(p, h)) {
                return;
            }
        }
        boolean hit = p.getDecisionHitOrStand();

        if (this.enableTrainer) {
            log.info("The correct action is {}\n", Trainer.getBestAction(h, this.dealer.getHand()).name());
        }

        if (hit) {
            h.addCard(this.pack.drawCard());
            this.playHand(p, h);
        }
    }

    private void handleSplitAce(Player p, Hand h) {
        h.addCard(this.pack.drawCard());
        h.printHand();
        this.dealer.printDealer();

        if (h.isBlackjack()) {
            log.info("Mini blackjack from split ace!");
            this.payPlayer(p, h.getCurrentBet() * 2);
        }
    }

    private boolean split(Player p, Hand h) {
        log.info("Would you like to split?");
        boolean yes = p.getDecisionYesOrNo();

        if (this.enableTrainer) {
            Action bestAction = Trainer.getBestAction(h, this.dealer.getHand());
            if (bestAction == Action.SPLIT) {
                log.info("The correct choice is YES\n");
            } else {
                log.info("The correct choice is NO\n");
            }
        }

        if (!yes) {
            return false;
        }
        try {
            p.deductMoney(h.getCurrentBet());
        } catch (Exception ex) {
            log.info(ex.getMessage());
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
        this.dealer.printDealer();

        log.info("Would you like to double down, " + p.getName() + "?");
        boolean yes = p.getDecisionYesOrNo();

        if (this.enableTrainer) {
            Action bestAction = Trainer.getBestAction(p.getHand(0), this.dealer.getHand());
            if (bestAction == Action.DOUBLE_DOWN) {
                log.info("The correct choice is YES\n");
            } else {
                log.info("The correct choice is NO\n");
            }
        }

        if (!yes) {
            return false;
        }

        try {
            Hand h = p.getHand(0);
            p.deductMoney(h.getCurrentBet());
            h.setCurrentBet(h.getCurrentBet() * 2);
            h.setDoubleDownCard(this.pack.drawCard());
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }
        return true;
    }

    private void resolveDoubleDowns() {
        for (Player p : this.players) {
            Card ddc = p.getHand(0).getDoubleDownCard();
            if (ddc != null) {
                log.info("Resolving double down...");
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
            log.info("Dealer hits...");
            this.dealer.getHand().addCard(this.pack.drawCard());
            this.playDealer();
            return;
        }
        if (high > 21) {
            log.info("Dealer busted!");
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
                             log.info(String.format("%s beat the dealer! (%d vs. %d)\n", p.getName(), pHigh, dealerHigh));
                             this.payPlayer(p, h.getCurrentBet() * 2);
                         } else if (pHigh == dealerHigh) {
                             log.info(String.format("%s tied the dealer. (%d vs. %d)\n", p.getName(), pHigh, dealerHigh));
                             this.payPlayer(p, h.getCurrentBet());
                         } else {
                             log.info(String.format("%s lost to the dealer. (%d vs. %d)\n", p.getName(), pHigh, dealerHigh));
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
        log.info(String.format("%s made $%d!\n", p.getName(), amount));
    }

    private void printGame() {
        this.dealer.printDealer();
        for (Player p : this.players) {
            p.printPlayer();
        }
    }

    public static void main(String[] args) {
        Config config = ConfigFactory.load("blackjack");
        
        int numPlayers = config.getInt("num_players");
        List<String> names = config.getStringList("player_names");
        List<Integer> startingMoney = config.getIntList("player_money");
        boolean enableTrainer = config.getBoolean("enable_trainer");

        Game game = new Game(enableTrainer);
        for (int i = 0; i < numPlayers; ++i) {
            game.addPlayer(new Player(names.get(i), startingMoney.get(i)));
        }
        game.nextRound();
    }
}
