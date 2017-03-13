package com.tom_e_white.set_game;

import java.io.File;
import java.io.IOException;

/**
 * Runs tests on raw training images to check that 3 x 9 cards can be detected. It it fails, the
 * errant image is displayed.
 */
public class CheckRawTrainingImagesV2 {
    public static void main(String[] args) throws IOException {
        CardDetector cardDetector = new CardDetector(4, 66);
        for (File file : new File("data/train-v2/raw").listFiles((dir, name) -> name.matches(".*\\.jpg"))) {
            try {
                System.out.println(file);
                cardDetector.detect(file.getAbsolutePath(), false, true, 3, 9);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                cardDetector.detect(file.getAbsolutePath(), true, true);
                break;
            }
        }
    }
}
