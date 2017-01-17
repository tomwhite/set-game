package com.tom_e_white.set_game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Take the images created by {@link CreateTrainingSet} and extract useful features from the polygons in them.
 * The resulting data can be used a to train a model for predicting the shapes on a card.
 */
public class CreateShapeTrainingData {
    public static void main(String[] args) throws IOException {
        Stream<String> stream = Arrays.stream(new File("data/train-out").listFiles((dir, name) -> name.matches(".*\\.jpg")))
                .map(file -> {
                            try {
                                FindCardShapeFeatures findCardShapeFeatures = new FindCardShapeFeatures();
                                FindCardShapeFeatures.CardShapeFeatures cardShapeFeatures = findCardShapeFeatures.scan(file.getAbsolutePath());
                                StringBuilder sb = new StringBuilder();
                                sb.append(getShapeNumber(file)).append(",");
                                sb.append(cardShapeFeatures.getNumSides()).append(",");
                                sb.append(cardShapeFeatures.isConvex() ? "1" : "0");
                                return sb.toString();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        Path p = Paths.get("data/train-out-shape.txt");
        Files.write(p, (Iterable<String>)stream::iterator);
    }

    private static int getShapeNumber(File file) {
        String shapeString = CardPredictor.toLabel(file).split(" ")[3];
        switch (shapeString) {
            case "oval": case "ovals": return 1;
            case "diamond": case "diamonds": return 2;
            case "squiggle": case "squiggles": return 3;
            default: throw new IllegalArgumentException("Unrecognized shape: " + shapeString);
        }
    }
}
