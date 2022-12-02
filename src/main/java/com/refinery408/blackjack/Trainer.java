package com.refinery408.blackjack;

public class Trainer {
    public static Action getBestAction(Hand playerHand, Hand dealerHand) {
        if (playerHand.hasTwinRanks()) {
            return handleTwins(playerHand, dealerHand);
        }
        if (playerHand.hasAceAndAnother()) {
            return handleAce(playerHand, dealerHand);
        }
        return handleSum(playerHand, dealerHand);
    }

    private static Action handleTwins(Hand playerHand, Hand dealerHand) {
        switch (playerHand.getCard(0).getRank()) {
            case TWO:
            case THREE:
            case SEVEN:
                switch (dealerHand.getCard(0).getRank()) {
                    case TWO:
                    case THREE:
                    case FOUR:
                    case FIVE:
                    case SIX:
                    case SEVEN:
                        return Action.SPLIT;
                    default:
                        return Action.HIT;
                }
            case FOUR:
                switch (dealerHand.getCard(0).getRank()) {
                    case FIVE:
                    case SIX:
                        return Action.SPLIT;
                    default:
                        return Action.HIT;
                }
            case FIVE:
                switch (dealerHand.getCard(0).getRank()) {
                    case TWO:
                    case THREE:
                    case FOUR:
                    case FIVE:
                    case SIX:
                    case SEVEN:
                    case EIGHT:
                    case NINE:
                        return Action.DOUBLE_DOWN;
                    default:
                        return Action.HIT;
                }
            case SIX:
                switch (dealerHand.getCard(0).getRank()) {
                    case TWO:
                    case THREE:
                    case FOUR:
                    case FIVE:
                    case SIX:
                        return Action.SPLIT;
                    default:
                        return Action.HIT;
                }
            case EIGHT:
            case ACE:
                return Action.SPLIT;
            case NINE:
                switch (dealerHand.getCard(0).getRank()) {
                    case TWO:
                    case THREE:
                    case FOUR:
                    case FIVE:
                    case SIX:
                    case EIGHT:
                    case NINE:
                        return Action.SPLIT;
                    default:
                        return Action.STAND;
                }
            case TEN:
            case JACK:
            case QUEEN:
            case KING:
                return Action.STAND;
            default:
                return null;
        }
    }

    private static Action handleAce(Hand playerHand, Hand dealerHand) {
        Hand withoutAce = playerHand.newWithoutAce();

        switch (withoutAce.lowValue()) {
            case 2:
            case 3:
                switch (dealerHand.getCard(0).getRank()) {
                    case FIVE:
                    case SIX:
                        return Action.DOUBLE_DOWN;
                    default:
                        return Action.HIT;
                }
            case 4:
            case 5:
                switch (dealerHand.getCard(0).getRank()) {
                    case FOUR:
                    case FIVE:
                    case SIX:
                        return Action.DOUBLE_DOWN;
                    default:
                        return Action.HIT;
                }
            case 6:
                switch (dealerHand.getCard(0).getRank()) {
                    case THREE:
                    case FOUR:
                    case FIVE:
                    case SIX:
                        return Action.DOUBLE_DOWN;
                    default:
                        return Action.HIT;
                }
            case 7:
                switch (dealerHand.getCard(0).getRank()) {
                    case TWO:
                    case SEVEN:
                    case EIGHT:
                        return Action.STAND;
                    case THREE:
                    case FOUR:
                    case FIVE:
                    case SIX:
                        return Action.DOUBLE_DOWN;
                    default:
                        return Action.HIT;
                }
            case 8:
            case 9:
            case 10:
                return Action.STAND;
            default:
                return null;
        }
    }

    private static Action handleSum(Hand playerHand, Hand dealerHand) {
        switch (playerHand.lowValue()) {
            case 5:
            case 6:
            case 7:
            case 8:
                return Action.HIT;
            case 9:
                switch (dealerHand.getCard(0).getRank()) {
                    case THREE:
                    case FOUR:
                    case FIVE:
                    case SIX:
                        return Action.DOUBLE_DOWN;
                    default:
                        return Action.HIT;
                }
            case 10:
                switch (dealerHand.getCard(0).getRank()) {
                    case TWO:
                    case THREE:
                    case FOUR:
                    case FIVE:
                    case SIX:
                    case SEVEN:
                    case EIGHT:
                    case NINE:
                        return Action.DOUBLE_DOWN;
                    default:
                        return Action.HIT;
                }
            case 11:
                switch (dealerHand.getCard(0).getRank()) {
                    case ACE:
                        return Action.HIT;
                    default:
                        return Action.DOUBLE_DOWN;
                }
            case 12:
                switch (dealerHand.getCard(0).getRank()) {
                    case FOUR:
                    case FIVE:
                    case SIX:
                        return Action.STAND;
                    default:
                        return Action.HIT;
                }
            case 13:
            case 14:
            case 15:
            case 16:
                switch (dealerHand.getCard(0).getRank()) {
                    case TWO:
                    case THREE:
                    case FOUR:
                    case FIVE:
                    case SIX:
                        return Action.STAND;
                    default:
                        return Action.HIT;
                }
            default:
                return Action.STAND;
        }
    }
}
