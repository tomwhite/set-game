package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.model.Cards;
import com.tom_e_white.set_game.model.Triple;
import com.tom_e_white.set_game.preprocess.CardDetector;
import com.tom_e_white.set_game.preprocess.CardImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use {@link CardPredictor} to predict Sets in the test set.
 */
public class PredictSetsOnTestData {

    public static double predict(List<File> testFiles, CardPredictor cardPredictor)
        throws IOException, ParseException {
        int correct = 0;
        int total = 0;
        for (File testFile : testFiles) {
            if (predict(testFile, cardPredictor)) {
                correct++;
            } else {
                System.out.println("Incorrect for file " + testFile);
            }
            total++;
        }
        System.out.println("Correct: " + correct);
        System.out.println("Total: " + total);
        double accuracy = ((double) correct)/total * 100;
        System.out.println("Accuracy: " + ((int) accuracy) + " percent");
        System.out.println("------------------------------------------");
        return accuracy;

    }

    private static boolean predict(File testFile, CardPredictor cardPredictor) throws
        IOException, ParseException {
        CardDetector cardDetector = new CardDetector(66);
        List<CardImage> images = cardDetector.detect(testFile.getAbsolutePath(),false, true);
        List<String> testDescriptions = Files.lines(
            Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt").replace(".png", ".txt")))
            .collect(Collectors.toList());
        List<Card> layout = testDescriptions.stream().map(Card::new).collect(Collectors.toList());
        Set<Triple> sets = Cards.sets(layout);

        List<CardPrediction> cardPredictions = images.stream().map(cardImage -> {
            try {
                return cardPredictor.predict(cardImage);
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        SetPredictor setPredictor = new SetPredictor();
        List<SetPrediction> predictions = setPredictor.predict(cardPredictions);
        System.out.println(predictions);

        boolean correct;
        if (predictions.isEmpty()) {
            correct = sets.isEmpty();
            if (!correct) {
                System.out.println("Incorrect, predicted no sets but actual sets are " + sets);
            }
        } else {
            Triple bestSet = predictions.get(0).getSet();
            correct = sets.contains(bestSet);
            if (!correct) {
                System.out.println("Incorrect, predicted " + bestSet + " but actual sets are " + sets);
            }
        }
        return correct;
    }

    public static void main(String[] args) throws Exception {
        predict(new File(args[0]), PredictCards.getCardPredictor(args.length > 1 ? args[1] : null));
    }
}
