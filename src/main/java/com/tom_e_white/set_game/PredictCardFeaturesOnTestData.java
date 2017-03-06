package com.tom_e_white.set_game;

import smile.classification.Classifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use feature finders to predict the features of each card in the test set.
 */
public class PredictCardFeaturesOnTestData {

    public static double[] predict(File testFile) throws IOException, ParseException {

        FeatureFinder[] finders = new FeatureFinder[] {
                new FindCardNumberFeatures(),
                new FindCardColourFeatures(),
                new FindCardShadingFeatures(),
                new FindCardShapeFeatures()
        };
        double[] accuracies = new double[finders.length];
        int index = 0;
        for (FeatureFinder finder : finders) {
            System.out.println(finder.getClass().getSimpleName());

            Classifier<double[]> classifier = finder.getClassifier();

            CardDetector cardDetector = new CardDetector();
            List<CardImage> images = cardDetector.detect(testFile.getAbsolutePath(), false);
            List<String> testDescriptions = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());

            int correct = 0;
            int total = 0;
            for (int i = 0; i < testDescriptions.size(); i++) {
                double[] features = finder.find(images.get(i).getImage(), false);
                int predictedLabel = classifier.predict(features);
                int actualLabel = finder.getLabelFromDescription(testDescriptions.get(i));
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
            accuracies[index++] = accuracy;
            System.out.println("Accuracy: " + ((int) accuracy) + " percent");
            System.out.println("------------------------------------------");
        }
        return accuracies;
    }

    public static void main(String[] args) throws Exception {
        predict(new File(args[0]));
    }
}
