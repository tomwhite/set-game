package com.tom_e_white.set_game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Take the images created by {@link CreateTrainingSet} and extract useful features from the polygons in them.
 * The resulting data can be used a to train a model for predicting the shapes on a card.
 */
public class CreateShadingTrainingData {
    public static void main(String[] args) throws IOException {
        FeatureFinder<? extends Features> finder = new FindCardShadingFeatures();
        Stream<String> stream = Arrays.stream(new File("data/train-out").listFiles((dir, name) -> name.matches(".*\\.jpg")))
                .map(file -> {
                            try {
                                Features features = finder.find(file.getAbsolutePath(), false);
                                return features == null ? "" : features.getSummaryLine();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        Path p = Paths.get("data/train-out-shading.csv");
        Files.write(p, (Iterable<String>)stream::iterator);
    }

}
