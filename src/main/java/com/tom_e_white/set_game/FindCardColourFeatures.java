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
            sb.append(i).append(":").append(String.format("%.4f", features[i])).append(" ");
        }
        return sb.toString();
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
