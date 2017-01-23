package com.tom_e_white.set_game;

import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.image.ImageUtils;

import java.io.File;
import java.io.IOException;

public class FindCardColourFeatures implements FeatureFinder<FindCardColourFeatures.CardColourFeatures>{

    public static class CardColourFeatures implements Features {

        private final int label;
        private final double[] vector;

        public CardColourFeatures(int label, double[] vector) {
            this.label = label;
            this.vector = vector;
        }

        @Override
        public String getSummaryLine() {
            StringBuilder sb = new StringBuilder();
            sb.append(label).append(" ");
            for (int i = 0; i < vector.length; i++) {
                sb.append(i).append(":").append(String.format("%.4f", vector[i])).append(" ");
            }
            return sb.toString();        }
    }

    @Override
    public CardColourFeatures find(String filename, boolean debug) throws IOException {
        double[] vector = ImageUtils.coupledHueSat(UtilImageIO.loadImage(filename));
        return new CardColourFeatures(CardLabel.getColourNumber(new File(filename)), vector);
    }

    @Override
    public String getFileName() {
        return "colour.svm";
    }
}
