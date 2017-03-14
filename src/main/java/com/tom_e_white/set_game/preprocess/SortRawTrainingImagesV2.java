package com.tom_e_white.set_game.preprocess;

import com.tom_e_white.set_game.train.FindCardNumberFeatures;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static com.tom_e_white.set_game.preprocess.TrainingDataV2.RAW_NEW_DIRECTORY;
import static com.tom_e_white.set_game.preprocess.TrainingDataV2.RAW_SORTED_DIRECTORY;

/**
 * Uses {@link FindCardNumberFeatures} to sort the raw training images by the number of shapes on each card (board).
 * Images are moved to <i>raw-sorted/&lt;num<gt;</i> directories, where they can be visually checked by a human.
 */
public class SortRawTrainingImagesV2 {
    public static void main(String[] args) throws IOException {
        RAW_SORTED_DIRECTORY.mkdirs();
        FindCardNumberFeatures cardFeatureCounter = new FindCardNumberFeatures();
        CardDetector cardDetector = new CardDetector(4, 66);
        for (File file : RAW_NEW_DIRECTORY.listFiles((dir, name) -> name.matches(".*\\.jpg"))) {
            System.out.println(file);
            List<CardImage> cardImages = cardDetector.detect(file.getAbsolutePath(), false, true, 3, 9);
            Map<Integer, Integer> counts = new HashMap<>();
            for (CardImage cardImage : cardImages) {
                int predictedNumber = (int) cardFeatureCounter.find(cardImage.getImage(), false)[0];
                Integer oldCount = counts.getOrDefault(predictedNumber, 0);
                counts.put(predictedNumber, oldCount + 1);
            }
            int modalNumber = counts.entrySet().stream()
                    .max(Comparator.comparing(Map.Entry::getValue))
                    .get()
                    .getKey();
            File targetDir = new File(RAW_SORTED_DIRECTORY, Integer.toString(modalNumber));
            targetDir.mkdirs();
            Files.move(file.toPath(), new File(targetDir, file.getName()).toPath());
        }
    }
}
