package com.tom_e_white.set_game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Take the images created by {@link CreateTrainingSet} and extract useful features from them.
 * The resulting data can be used to train a model for predicting the shapes on a card.
 */
public class CreateTestData {
    public static void main(String[] args) throws IOException {
        File testFile = new File("data/20170106_205743.jpg");
        FeatureFinder[] finders = new FeatureFinder[] {
                new FindCardColourFeatures(),
                new FindCardShadingFeatures(),
                new FindCardShapeFeatures()
        };
        for (FeatureFinder finder : finders) {
            List<String> summaries = new ArrayList<>();
            CardDetector cardDetector = new CardDetector();
            List<BufferedImage> images = cardDetector.scan(testFile.getAbsolutePath(), false);
            List<String> testLabels = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());
            for (int i = 0; i < testLabels.size(); i++) {
                double[] features = finder.find(images.get(i), false);
                int label = finder.getLabelNumberFromLabel(testLabels.get(i));
                if (features != null) {
                    summaries.add(finder.getSummaryLine(label, features));
                }
            }
            Path p = Paths.get("data/test-out-" + finder.getFileName());
            Files.write(p, summaries);
        }
    }

}
