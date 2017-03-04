package com.tom_e_white.set_game;

import smile.classification.Classifier;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;

public abstract class FeatureFinder {
    public abstract int getLabel(String filename);
    public abstract int getLabelNumberFromLabel(String label);
    public abstract double[] find(BufferedImage image, boolean debug) throws IOException;
    public abstract String getFileSuffix();
    public abstract Classifier<double[]> getClassifier() throws IOException, ParseException;

    public String getSummaryLine(int label, double[] features) {
        StringBuilder sb = new StringBuilder().append(label);
        for (double f : features) {
            sb.append(",").append(f);
        }
        return sb.toString();
    }

    public String getSummaryLine(String filename, double[] features) {
        return getSummaryLine(getLabel(filename), features);
    }
}
