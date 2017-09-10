package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.predict.CardPredictor;
import com.tom_e_white.set_game.preprocess.CardDetector;
import com.tom_e_white.set_game.preprocess.CardImage;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Use {@link CardPredictor} to predict cards (with no descriptions).
 */
public class PredictCards {

    public static void predict(File testFile) throws IOException, ParseException {
        CardDetector cardDetector = new CardDetector();
        CardPredictor cardPredictor = new CardPredictor();
        List<CardImage> images = cardDetector.detect(testFile.getAbsolutePath(), false, true);
        images.stream().map(cardImage -> {
            try {
                return cardPredictor.predict(cardImage.getImage());
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }).forEach(cp -> System.out.println(cp.getCard()));
    }

    public static void main(String[] args) throws Exception {
        predict(new File(args[0]));
    }
}
