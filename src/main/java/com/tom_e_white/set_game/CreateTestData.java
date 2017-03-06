package com.tom_e_white.set_game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Takes test images and extract useful features from them.
 * The resulting data can be used to test the predictive accuracy of the model from R.
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
            List<CardImage> images = cardDetector.detect(testFile.getAbsolutePath(), false);
            List<String> testDescriptions = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());
            for (int i = 0; i < testDescriptions.size(); i++) {
                double[] features = finder.find(images.get(i).getImage(), false);
                int label = finder.getLabelFromDescription(testDescriptions.get(i));
                if (features != null) {
                    summaries.add(finder.getSummaryLine(label, features));
                }
            }
            Path p = Paths.get("data/test-out-" + finder.getFileSuffix());
            Files.write(p, summaries);
        }
    }

}
