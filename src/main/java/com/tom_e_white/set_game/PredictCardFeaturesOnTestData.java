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
 * Use feature finders to predict the features of each card in the test set.
 */
public class PredictCardFeaturesOnTestData {

    public static void predict(File testFile) throws IOException, ParseException {

        FeatureFinder[] finders = new FeatureFinder[] {
                new FindCardColourFeatures(),
                new FindCardShadingFeatures(),
                new FindCardShapeFeatures()
        };
        for (FeatureFinder finder : finders) {
            System.out.println(finder.getClass().getSimpleName());

            Classifier<double[]> classifier = finder.getClassifier();

            CardDetector cardDetector = new CardDetector();
            List<BufferedImage> images = cardDetector.scan(testFile.getAbsolutePath(), false);
            List<String> testLabels = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());

            int correct = 0;
            int total = 0;
            for (int i = 0; i < testLabels.size(); i++) {
                double[] features = finder.find(images.get(i), false);
                int predictedLabel = classifier.predict(features) + 1; // add one as our labels are 1-based
                int actualLabel = finder.getLabelNumberFromLabel(testLabels.get(i));
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
            System.out.println("------------------------------------------");
        }
    }

    public static void main(String[] args) throws Exception {
        predict(new File(args[0]));
    }
}
