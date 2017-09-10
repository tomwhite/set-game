package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.model.Cards;
import com.tom_e_white.set_game.model.Triple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Find the best Sets from a list of card predictions, using probability to guide the choice.
 */
public class SetFinder {
    public List<ScoredSet> findSets(List<CardPrediction> cardPredictions) {
        List<Card> cards = cardPredictions.stream()
                .map(CardPrediction::getCard)
                .collect(Collectors.toList());
        Set<Triple> sets = Cards.sets(cards);

        Map<Card, Float> cardToProbabilityMap = new LinkedHashMap<>();
        for (CardPrediction cardPrediction : cardPredictions) {
            cardToProbabilityMap.put(cardPrediction.getCard(), cardPrediction.getProbability());
        }

        List<ScoredSet> scoredSets = new ArrayList<>();
        for (Triple set : sets) {
            scoredSets.add(new ScoredSet(set, score(set, cardToProbabilityMap)));
        }
        Collections.sort(scoredSets);
        return scoredSets;
    }

    private static float score(Triple set, Map<Card, Float> probs) {
        // geometric mean (no need to take root, since there are always three cards in a set)
        return probs.get(set.first()) * probs.get(set.second()) * probs.get(set.third());
    }
}
