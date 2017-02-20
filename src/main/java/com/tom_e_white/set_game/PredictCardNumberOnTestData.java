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
 * Use {@link FindCardShapeFeatures} to predict the shape of each card in the test set.
 */
public class PredictCardNumberOnTestData {

    public static double predict(File testFile) throws IOException, ParseException {
        CardDetector cardDetector = new CardDetector();
        List<BufferedImage> images = cardDetector.scan(testFile.getAbsolutePath(), true);
        List<String> testLabels = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());

        int correct = 0;
        int total = 0;
        FindCardNumberFeatures featureFinder = new FindCardNumberFeatures();
        for (int i = 0; i < testLabels.size(); i++) {
            int predictedNumber = featureFinder.scan(images.get(i), false);
            int actualNumber = CardLabel.getNumber(testLabels.get(i));
            if (predictedNumber == actualNumber) {
                correct++;
            } else {
                System.out.println("Incorrect, predicted " + predictedNumber + " but was " + actualNumber + " for card " + (i + 1));
            }
            total++;
        }
        System.out.println("Correct: " + correct);
        System.out.println("Total: " + total);
        double accuracy = ((double) correct)/total * 100;
        System.out.println("Accuracy: " + ((int) accuracy) + " percent");
        return accuracy;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(predict(new File(args[0])));
    }
}
