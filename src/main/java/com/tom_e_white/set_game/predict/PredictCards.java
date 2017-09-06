package com.tom_e_white.set_game.predict;

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

    public static void predict(File testFile, CardPredictor cardPredictor) throws IOException, ParseException {
        CardDetector cardDetector = new CardDetector();
        List<CardImage> images = cardDetector.detect(testFile.getAbsolutePath(), false, true);
        images.stream().map(cardImage -> {
            try {
                return cardPredictor.predict(cardImage);
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }).forEach(cp -> System.out.println(cp.getCard()));
    }

    static CardPredictor getCardPredictor(String simpleName)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (simpleName == null) {
            return new CardPredictor();
        }
        Class<CardPredictor> c =
            (Class<CardPredictor>) Class.forName(CardPredictor.class.getPackage().getName() + "." + simpleName);
        return c.newInstance();
    }

    public static void main(String[] args) throws Exception {
        predict(new File(args[0]), getCardPredictor(args.length > 1 ? args[1] : null));
    }
}
