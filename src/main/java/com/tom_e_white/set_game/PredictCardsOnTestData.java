package com.tom_e_white.set_game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use feature finders to predict each card in the test set.
 */
public class PredictCardsOnTestData {

    public static double predict(File testFile) throws IOException, ParseException {

        FeatureFinder numberFinder = new FindCardNumberFeatures();
        FeatureFinder shadingFinder = new FindCardShadingFeatures();
        FeatureFinder colourFinder = new FindCardColourFeatures();
        FeatureFinder shapeFinder = new FindCardShapeFeatures();

        CardDetector cardDetector = new CardDetector();
        List<BufferedImage> images = cardDetector.scan(testFile.getAbsolutePath(), false);
        List<String> testDescriptions = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());

        int correct = 0;
        int total = 0;
        for (int i = 0; i < testDescriptions.size(); i++) {
            BufferedImage image = images.get(i);
            Card predictedCard = new Card(predict(numberFinder, image), predict(shadingFinder, image),
                    predict(colourFinder, image), predict(shapeFinder, image));
            Card actualCard = new Card(testDescriptions.get(i));
            if (predictedCard.equals(actualCard)) {
                correct++;
            } else {
                System.out.println("Incorrect, predicted " + predictedCard + " but was " + actualCard + " for card " + (i + 1));
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

    private static int predict(FeatureFinder finder, BufferedImage image) throws IOException, ParseException {
        double[] features = finder.find(image, false);
        return finder.getClassifier().predict(features);
    }

    public static void main(String[] args) throws Exception {
        predict(new File(args[0]));
    }
}
