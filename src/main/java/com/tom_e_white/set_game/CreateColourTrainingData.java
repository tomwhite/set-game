package com.tom_e_white.set_game;

import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.image.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Take the images created by {@link CreateTrainingSet} and extract Hue and Saturation histograms from them.
 * The resulting data can be used a to train a model (using kNN or SVM) for predicting the colour of a card.
 */
public class CreateColourTrainingData {
    public static void main(String[] args) throws IOException {
        Stream<String> stream = Arrays.stream(new File("data/train-out").listFiles((dir, name) -> name.matches(".*\\.jpg")))
                .map(file -> {
                    double[] vector = ImageUtils.coupledHueSat(UtilImageIO.loadImage(file.getAbsolutePath()));
                    StringBuilder sb = new StringBuilder();
                    sb.append(CardLabel.getColourNumber(file)).append(" ");
                    for (int i = 0; i < vector.length; i++) {
                        sb.append(i).append(":").append(String.format("%.4f", vector[i])).append(" ");
                    }
                    return sb.toString();
                }
        );
        Path p = Paths.get("data/train-out-svm.txt");
        Files.write(p, (Iterable<String>)stream::iterator);
    }
}
