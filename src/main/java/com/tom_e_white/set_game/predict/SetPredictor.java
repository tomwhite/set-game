package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.model.Cards;
import com.tom_e_white.set_game.model.Triple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Predict the best Sets from a list of card predictions, using probability to guide the
 * choice.
 */
public class SetPredictor {
    public List<SetPrediction> predict(List<CardPrediction> cardPredictions) {
        List<Card> cards = cardPredictions.stream()
                .map(CardPrediction::getCard)
                .collect(Collectors.toList());
        Set<Triple> sets = Cards.sets(cards);

        Map<Card, Float> cardToProbabilityMap = new LinkedHashMap<>();
        for (CardPrediction cardPrediction : cardPredictions) {
            cardToProbabilityMap.put(cardPrediction.getCard(), cardPrediction.getProbability());
        }

        List<SetPrediction> predictions = new ArrayList<>();
        for (Triple set : sets) {
            predictions.add(new SetPrediction(set, geometricMean(set, cardToProbabilityMap)));
        }
        Collections.sort(predictions);
        return predictions;
    }

    private static float geometricMean(Triple set, Map<Card, Float> probs) {
        float p1 = probs.get(set.first());
        float p2 = probs.get(set.second());
        float p3 = probs.get(set.third());
        if (p1 == 1.0f && p2 == 1.0f && p3 == 1.0f) {
            return 1.0f;
        }
        return (float) Math.pow(p1 * p2 * p3, 1/3);
    }
}
