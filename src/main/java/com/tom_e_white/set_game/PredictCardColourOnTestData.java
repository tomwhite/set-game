package com.tom_e_white.set_game;

import smile.classification.Classifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use {@link FindCardColourFeatures} to predict the colour of each card in the test set.
 */
public class PredictCardColourOnTestData {

    public static double predict(File testFile) throws IOException, ParseException {
        FindCardColourFeatures featureFinder = new FindCardColourFeatures();
        Classifier<double[]> classifier = featureFinder.getClassifier();

        CardDetector cardDetector = new CardDetector();
        List<BufferedImage> images = cardDetector.scan(testFile.getAbsolutePath(), false);
        List<String> testLabels = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());

        int correct = 0;
        int total = 0;
        for (int i = 0; i < testLabels.size(); i++) {
            double[] features = featureFinder.find(images.get(i), false);
            int predictedLabel = classifier.predict(features) + 1; // add one as our labels are 1-based
            int actualLabel = featureFinder.getLabelNumberFromLabel(testLabels.get(i));
            if (predictedLabel == actualLabel) {
                correct++;
            } else {
                System.out.println("Incorrect, predicted " + predictedLabel + " but was " + actualLabel + " for card " + (i + 1));
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
