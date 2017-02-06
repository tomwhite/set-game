package com.tom_e_white.set_game;

import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;
import smile.classification.KNN;
import smile.data.AttributeDataset;
import smile.data.NominalAttribute;
import smile.data.SparseDataset;
import smile.data.parser.DelimitedTextParser;
import smile.data.parser.LibsvmParser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use {@link FindCardColourFeatures} to predict the colour of each card in the test set.
 */
public class PredictCardColourOnTestData {


    public static double predict(File testFile) throws IOException, ParseException {
        LibsvmParser parser = new LibsvmParser();
        SparseDataset dataset = parser.parse("data/train-out-colour.svm");
        double[][] vectors = dataset.toArray();
        int[] label = dataset.toArray(new int[dataset.size()]);

        CardDetector cardDetector = new CardDetector();
        List<BufferedImage> images = cardDetector.scan(testFile.getAbsolutePath(), true);
        List<String> testLabels = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());

        int correct = 0;
        int total = 0;
        FindCardColourFeatures featureFinder = new FindCardColourFeatures();
        for (int i = 0; i < testLabels.size(); i++) {
            double[] features = featureFinder.find(images.get(i), false);

            KNN<double[]> knn = KNN.learn(vectors, label, 25);
            int predictedLabel = knn.predict(features) + 1; // add one as our labels are 1-based
            int actualLabel = CardLabel.getColourNumber(testLabels.get(i));
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
