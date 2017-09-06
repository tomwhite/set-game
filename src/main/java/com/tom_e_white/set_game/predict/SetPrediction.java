package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Triple;

/**
 * A Set with an associated probability that it was found in a layout.
 */
public class SetPrediction implements Comparable<SetPrediction> {
    private final Triple set;
    private final float probability;

    public SetPrediction(Triple set, float probability) {
        this.set = set;
        this.probability = probability;
    }

    public Triple getSet() {
        return set;
    }

    public float getProbability() {
        return probability;
    }

    @Override
    public String toString() {
        return String.format("%s (p=%.2f)", set, probability);
    }

    @Override
    public int compareTo(SetPrediction o) {
        return -Float.compare(probability, o.probability);
    }
}
