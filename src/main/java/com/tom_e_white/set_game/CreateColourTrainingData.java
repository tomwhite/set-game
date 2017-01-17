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
 * Take the images created by {@link CreateTrainingSet} and extract Hue and Saturation histograms from them.
 * The resulting data can be used a to train a model (using kNN or SVM) for predicting the colour of a card.
 */
public class CreateColourTrainingData {
    public static void main(String[] args) throws IOException {
        Stream<String> stream = Arrays.stream(new File("data/train-out").listFiles((dir, name) -> name.matches(".*\\.jpg")))
                .map(file -> {
                    List<File> fs = new ArrayList<>();
                    fs.add(file);
                    double[] vector = ExampleColorHistogramLookup.coupledHueSat(fs).get(0);
                    StringBuilder sb = new StringBuilder();
                    sb.append(getColourNumber(file)).append(" ");
                    for (int i = 0; i < vector.length; i++) {
                        sb.append(i).append(":").append(String.format("%.4f", vector[i])).append(" ");
                    }
                    return sb.toString();
                }
        );
        Path p = Paths.get("data/train-out-svm.txt");
        Files.write(p, (Iterable<String>)stream::iterator);
    }

    private static int getColourNumber(File file) {
        String colourString = CardPredictor.toLabel(file).split(" ")[2];
        switch (colourString) {
            case "red": return 1;
            case "purple": return 2;
            case "green": return 3;
            default: throw new IllegalArgumentException("Unrecognized colour: " + colourString);
        }
    }
}
