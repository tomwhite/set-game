package com.tom_e_white.set_game;

public class LabelledVector {
    private int label;
    private double[] vector;

    public LabelledVector(int label, double[] vector) {
        this.label = label;
        this.vector = vector;
    }

    public int getLabel() {
        return label;
    }

    public double[] getVector() {
        return vector;
    }
}
