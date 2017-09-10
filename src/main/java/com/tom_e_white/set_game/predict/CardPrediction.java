package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;

public class CardPrediction {
    private final Card card;
    private final float probability;

    public CardPrediction(Card card) {
        this(card, 1.0f);
    }

    public CardPrediction(Card card, float probability) {
        this.card = card;
        this.probability = probability;
    }

    public Card getCard() {
        return card;
    }

    public float getProbability() {
        return probability;
    }

    @Override
    public String toString() {
        return String.format("%s (p=%.2f)", card, probability);
    }
}
