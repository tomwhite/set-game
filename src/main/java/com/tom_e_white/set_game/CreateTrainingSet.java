package com.tom_e_white.set_game;

import boofcv.io.image.UtilImageIO;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Use {@link CardDetector} to create a training set of cards.
 */
public class CreateTrainingSet {
    public static void main(String[] args) {
        CardDetector cardDetector = new CardDetector();
        File outDir = new File("data/train-out");
        outDir.mkdirs();
        Arrays.stream(new File("data/train").listFiles((dir, name) -> name.matches(".*\\.jpg"))).forEach(
                file -> {
                    try {
                        System.out.println(file);
                        List<CardImage> images = cardDetector.detect(file.getAbsolutePath());
                        int i = 1;
                        for (CardImage image : images) {
                            File newFile = new File(outDir, file.getName().replace(".jpg", "_" + i++ + ".jpg"));
                            UtilImageIO.saveImage(image.getImage(), newFile.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
