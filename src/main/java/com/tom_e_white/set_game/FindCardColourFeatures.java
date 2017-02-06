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
    public String getSummaryLine(String filename, double[] features) {
        // libsvm format, *not* csv
        StringBuilder sb = new StringBuilder();
        sb.append(getLabel(filename)).append(" ");
        for (int i = 0; i < features.length; i++) {
            sb.append(i + 1).append(":").append(String.format("%.4f", features[i])).append(" ");
        }
        return sb.toString();
    }

    @Override
    public LabelledVector getLabelledVector(String summaryLine) {
        // libsvm format, *not* csv
        String[] split = summaryLine.split(" ");
        double[] vector = new double[split.length - 1];
        for (int i = 0; i < split.length - 1; i++) {
            vector[i] = Double.parseDouble(split[i + 1].split(":")[1]);
        }
        return new LabelledVector(Integer.parseInt(split[0]), vector);
    }

    @Override
    public double[] find(BufferedImage image, boolean debug) throws IOException {
        return ImageUtils.coupledHueSat(image);
    }

    @Override
    public String getFileName() {
        return "colour.svm";
    }
}
