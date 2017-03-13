package com.tom_e_white.set_game;

import com.tom_e_white.set_game.model.Card;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;

/**
 * Predict the card that appears in an image using trained models.
 */
public class CardPredictor {

    private final FeatureFinder numberFinder = new FindCardNumberFeatures();
    private final FeatureFinder shadingFinder = new FindCardShadingFeatures();
    private final FeatureFinder colourFinder = new FindCardColourFeatures();
    private final FeatureFinder shapeFinder = new FindCardShapeFeatures();

    public Card predict(BufferedImage image) throws IOException, ParseException {
        return new Card(predict(numberFinder, image), predict(shadingFinder, image),
                predict(colourFinder, image), predict(shapeFinder, image));
    }

    private int predict(FeatureFinder finder, BufferedImage image) throws IOException, ParseException {
        double[] features = finder.find(image, false);
        return finder.getClassifier().predict(features);
    }
}
