package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Triple;

/**
 * A Set with an associated confidence score (higher is better).
 */
public class ScoredSet implements Comparable<ScoredSet> {
    private final Triple set;
    private final float score;

    public ScoredSet(Triple set, float score) {
        this.set = set;
        this.score = score;
    }

    public Triple getSet() {
        return set;
    }

    public float getScore() {
        return score;
    }

    @Override
    public String toString() {
        return String.format("%s (score=%.2f)", set, score);
    }

    @Override
    public int compareTo(ScoredSet o) {
        return -Float.compare(score, o.score);
    }
}
