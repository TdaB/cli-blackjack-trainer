package com.refinery408.blackjack;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Player {
    private static final Pattern YES_PATTERN = Pattern.compile("Y|y|((Y|y)(E|e)(S|s))");
    private static final Pattern NO_PATTERN = Pattern.compile("N|n|((N|n)(O|o))");
    private static final Pattern HIT_PATTERN = Pattern.compile("H|h|((H|h)(I|i)(T|t))");
    private static final Pattern STAND_PATTERN = Pattern.compile("S|s|((S|s)(T|t)(A|a)(N|n)(D|d))");
    private List<Hand> hands;
    private String name;
    private int money;
    private int insurance = 0;

    public Player(String name, int money) {
        this.name = name;
        this.money = money;
    }

    protected List<Hand> getHands() {
        return hands;
    }

    protected void setHands(List<Hand> hands) {
        this.hands = hands;
    }

    protected Hand getHand(int i) {
        return this.hands.get(i);
    }

    protected void addHand(Hand h) {
        this.hands.add(h);
    }

    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected int getMoney() {
        return money;
    }

    protected void setMoney(int money) {
        this.money = money;
    }

    protected int getInsurance() {
        return insurance;
    }

    protected void setInsurance(int insurance) {
        this.insurance = insurance;
    }

    protected void addMoney(int amount) {
        this.money += amount;
    }

    protected void deductMoney(int amount) throws Exception {
        if (amount > this.money) {
            throw new Exception("Not enough money");
        }
        if (amount <= 0) {
            throw new Exception("Bet must be greater than 0");
        }
        this.money -= amount;
    }

    protected void collectBet() {
        System.out.println("How much would you like to bet, " + this.name + "?");
        System.out.println("Bank: " + this.money);
        Scanner in = new Scanner(System.in);
        int amount;
        try {
            amount = in.nextInt();
        } catch (Exception ex) {
            System.out.println("Failed to read input!");
            this.collectBet();
            return;
        }
        try {
            this.deductMoney(amount);
            this.getHand(0).setCurrentBet(amount);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            this.collectBet();
        }
    }

    protected void collectInsurance() {
        System.out.println("Would you like some scam insurance, " + this.name + "?");
        boolean yes = this.getDecisionYesOrNo();
        if (yes) {
            this.betInsurance();
        }
    }

    protected void betInsurance() {
        System.out.println("How much insurance?");
        System.out.println("Bank: " + this.money);
        int maxInsurance = this.getHand(0).getCurrentBet() / 2;
        System.out.println("Max insurance: " + maxInsurance);
        Scanner in = new Scanner(System.in);
        int amount;
        try {
            amount = in.nextInt();
        } catch (Exception ex) {
            System.out.println("Failed to read input!");
            this.betInsurance();
            return;
        }
        if (amount > maxInsurance) {
            System.out.println("Cannot bet more than max insurance amount!");
            this.collectInsurance();
            return;
        }
        try {
            this.deductMoney(amount);
            this.insurance = amount;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            this.collectInsurance();
        }
    }

    protected void printPlayer() {
        System.out.println(this.name);
        for (Hand h : this.hands) {
            h.printHand();
        }
    }

    protected boolean getDecisionHitOrStand() {
        System.out.println("Hit or stand?");
        Scanner in = new Scanner(System.in);
        String decision;
        try {
            decision = in.nextLine();
        } catch (Exception ex) {
            System.out.println("Failed to read input!");
            return this.getDecisionHitOrStand();
        }
        if (HIT_PATTERN.matcher(decision).matches()) {
            return true;
        } else if (STAND_PATTERN.matcher(decision).matches()) {
            return false;
        } else {
            System.out.println("Wtf did you want?");
            return this.getDecisionHitOrStand();
        }
    }

    protected boolean getDecisionYesOrNo() {
        Scanner in = new Scanner(System.in);
        String decision;
        try {
            decision = in.nextLine();
        } catch (Exception ex) {
            System.out.println("Failed to read input!");
            return getDecisionYesOrNo();
        }
        if (YES_PATTERN.matcher(decision).matches()) {
            return true;
        } else if (NO_PATTERN.matcher(decision).matches()) {
            return false;
        } else {
            System.out.println("Wtf did you want?");
            return this.getDecisionYesOrNo();
        }
    }
    
    protected boolean canDoubleDown() {
        return this.getHand(0).canDoubleDown() && this.getMoney() >= this.getHand(0).getCurrentBet();
    }

    protected boolean isActive() {
        for (Hand h : this.hands) {
            if (h.isActive()) {
                return true;
            }
        }
        return false;
    }
}
