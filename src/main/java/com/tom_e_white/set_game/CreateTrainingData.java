package com.tom_e_white.set_game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Take the images created by {@link CreateTrainingSet} and extract useful features from them.
 * The resulting data can be used to train a model for predicting the shapes on a card.
 */
public class CreateTrainingData {
    public static void main(String[] args) throws IOException {
        FeatureFinder<? extends Features>[] finders = new FeatureFinder[] {
                new FindCardColourFeatures(),
                new FindCardShadingFeatures(),
                new FindCardShapeFeatures()
        };
        for (FeatureFinder<? extends Features> finder : finders) {
            List<String> summaries = new ArrayList<>();
            for (File file : new File("data/train-out").listFiles((dir, name) -> name.matches(".*\\.jpg"))) {
                Features features = finder.find(file.getAbsolutePath(), false);
                if (features != null) {
                    summaries.add(features.getSummaryLine());
                }
            }
            Path p = Paths.get("data/train-out-" + finder.getFileName());
            Files.write(p, summaries);
        }
    }

}
