package com.tom_e_white.set_game.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility functions for working with {@link Card}s.
 */
public class Cards {
    /**
     * @param cards
     * @return all the {@link Triple}s that can be made from the given cards.
     */
    public static List<Triple> triples(List<Card> cards) {
        int[] ind = new int[3];
        for(int i = 0; i < 3; i++) {
            ind[i] = i;
        }

        List<Triple> triples = new ArrayList<Triple>();
        outer:
        while (true) {
            Triple triple = new Triple(cards.get(ind[0]), cards.get(ind[1]), cards.get(ind[2]));
            triples.add(triple);
            for(int i = ind.length-1, j = cards.size()-1; i >= 0; i--, j--) {
                if(ind[i] != j) {
                    ind[i]++;
                    for(int k = i+1; k < ind.length; k++) {
                        ind[k] = ind[k-1]+1;
                    }
                    continue outer;
                }
            }
            break;
        }
        return triples;
    }

    public static boolean isASet(Triple triple) {
        return isASet(triple.first(), triple.second(), triple.third());
    }

    public static boolean isASet(Card c1, Card c2, Card c3) {
        return matches(c1.number(), c2.number(), c3.number())
                && matches(c1.shading(), c2.shading(), c3.shading())
                && matches(c1.color(), c2.color(), c3.color())
                && matches(c1.shape(), c2.shape(), c3.shape());
    }

    private static boolean matches(Enum<?> a, Enum<?> b, Enum<?> c) {
        return (a == b && b == c) || (a != b && b != c && c != a);
    }

    public static Set<Triple> sets(List<Card> cards) {
        Set<Triple> sets = new LinkedHashSet<>();
        for (Triple triple : triples(cards)) {
            if (isASet(triple)) {
                sets.add(triple);
            }
        }
        return sets;
    }
}
