package com.tom_e_white.set_game;

import smile.classification.Classifier;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;

public interface FeatureFinder {
    int getLabel(String filename);
    int getLabelNumberFromLabel(String label);
    double[] find(BufferedImage image, boolean debug) throws IOException;
    String getFileName();
    String getSummaryLine(int label, double[] features);
    String getSummaryLine(String filename, double[] features);
    LabelledVector getLabelledVector(String summaryLine);
    Classifier<double[]> getClassifier() throws IOException, ParseException;
}
