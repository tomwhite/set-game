package com.tom_e_white.set_game.preprocess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.tom_e_white.set_game.preprocess.TrainingDataV2.RAW_NEW_DIRECTORY;

/**
 * Runs tests on raw training images to check that 3 x 9 cards can be detected. It it fails, the
 * problem image is displayed, if in debug mode. All problem images are listed at the end.
 */
public class CheckRawTrainingImagesV2 {
    public static void main(String[] args) throws IOException {
        boolean debug = args.length > 0 && args[0].equals("--debug");
        CardDetector cardDetector = new CardDetector(4, 66);
        List<File> problemFiles = new ArrayList<>();
        for (File file : RAW_NEW_DIRECTORY.listFiles((dir, name) -> name.matches(".*\\.jpg"))) {
            try {
                System.out.println(file);
                cardDetector.detect(file.getAbsolutePath(), false, true, 3, 9);
            } catch (IllegalArgumentException e) {
                problemFiles.add(file);
                e.printStackTrace();
                if (debug) {
                    System.out.println("Debugging...");
                    cardDetector.detect(file.getAbsolutePath(), true, true);
                    break;
                }
            }
        }
        System.out.println("Problem files: " + problemFiles);
    }
}
