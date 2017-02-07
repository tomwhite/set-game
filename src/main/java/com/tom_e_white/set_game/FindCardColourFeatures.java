package com.tom_e_white.set_game;

import com.tom_e_white.set_game.image.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FindCardColourFeatures implements FeatureFinder {

    @Override
    public int getLabel(String filename) {
        return CardLabel.getColourNumber(new File(filename));
    }

    @Override
    public int getLabelNumberFromLabel(String label) {
        return CardLabel.getColourNumber(label);
    }

    @Override
    public String getSummaryLine(int label, double[] features) {
        StringBuilder sb = new StringBuilder().append(label);
        for (double f : features) {
            sb.append(",").append(f);
        }
        return sb.toString();
    }

    @Override
    public String getSummaryLine(String filename, double[] features) {
        return getSummaryLine(getLabel(filename), features);
    }

    @Override
    public LabelledVector getLabelledVector(String summaryLine) {
        String[] split = summaryLine.split(",");
        double[] vector = new double[split.length - 1];
        for (int i = 0; i < split.length - 1; i++) {
            vector[i] = Double.parseDouble(split[i + 1]);
        }
        return new LabelledVector(Integer.parseInt(split[0]), vector);
    }

    @Override
    public double[] find(BufferedImage image, boolean debug) throws IOException {
        return ImageUtils.coupledHueSat(image);
    }

    @Override
    public String getFileName() {
        return "colour.csv";
    }
}
