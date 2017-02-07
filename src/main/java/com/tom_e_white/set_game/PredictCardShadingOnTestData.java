package com.tom_e_white.set_game;

import smile.classification.KNN;
import smile.data.AttributeDataset;
import smile.data.NominalAttribute;
import smile.data.parser.DelimitedTextParser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use {@link FindCardShadingFeatures} to predict the shading on each card in the test set.
 */
public class PredictCardShadingOnTestData {

    public static double predict(File testFile) throws IOException, ParseException {
        DelimitedTextParser parser = new DelimitedTextParser();
        parser.setDelimiter(",");
        parser.setResponseIndex(new NominalAttribute("shading", new String[] { "1", "2", "3" }), 0);
        AttributeDataset dataset = parser.parse("data/train-out-shading.csv");
        double[][] vectors = dataset.toArray(new double[dataset.size()][]);
        int[] label = dataset.toArray(new int[dataset.size()]);

        CardDetector cardDetector = new CardDetector();
        List<BufferedImage> images = cardDetector.scan(testFile.getAbsolutePath(), true);
        List<String> testLabels = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());

        int correct = 0;
        int total = 0;
        FindCardShadingFeatures featureFinder = new FindCardShadingFeatures();
        for (int i = 0; i < testLabels.size(); i++) {
            double[] features = featureFinder.find(images.get(i), false);

            KNN<double[]> knn = KNN.learn(vectors, label, 5);
            int predictedLabel = knn.predict(features) + 1; // add one as our labels are 1-based
            int actualLabel = CardLabel.getShadingNumber(testLabels.get(i));
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
