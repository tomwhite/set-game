package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.preprocess.CardImage;
import com.tom_e_white.set_game.train.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;

/**
 * Predict the card that appears in an image of a single card using trained models.
 */
public class CardPredictor {

    private final FeatureFinder numberFinder;
    private final FeatureFinder colourFinder;
    private final FeatureFinder shadingFinder;
    private final FeatureFinder shapeFinder;

    public CardPredictor() {
        this(1);
    }

    protected CardPredictor(int version) {
        this.numberFinder = new FindCardNumberFeatures();
        this.colourFinder = new FindCardColourFeatures(version);
        this.shadingFinder = new FindCardShadingFeatures();
        this.shapeFinder = new FindCardShapeFeatures();
    }

    public CardPrediction predict(CardImage cardImage) throws IOException, ParseException {
        BufferedImage image = cardImage.getImage();
        return new CardPrediction(new Card(predict(numberFinder, image), predict(colourFinder, image),
                predict(shadingFinder, image), predict(shapeFinder, image)));
    }

    private int predict(FeatureFinder finder, BufferedImage image) throws IOException, ParseException {
        double[] features = finder.find(image, false);
        return finder.getClassifier().predict(features);
    }
}
