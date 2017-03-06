package com.tom_e_white.set_game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use {@link CardPredictor} to play Set.
 */
public class PlaySet {

    public static void play(File testFile) throws IOException, ParseException {
        CardDetector cardDetector = new CardDetector();
        CardPredictor cardPredictor = new CardPredictor();
        List<BufferedImage> images = cardDetector.scan(testFile.getAbsolutePath(), false);

        List<Card> cards = images.stream().map(image -> {
            try {
                return cardPredictor.predict(image);
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        Set<Triple> sets = Cards.sets(cards);

        sets.forEach(System.out::println);
    }

    public static void main(String[] args) throws Exception {
        play(new File(args[0]));
    }
}
