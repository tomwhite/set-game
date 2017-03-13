package com.tom_e_white.set_game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Uses {@link FindCardNumberFeatures} to sort the raw training images by the number of shapes on each card (board).
 * Images are moved to <i>raw-sorted/&lt;num<gt;</i> directories.
 */
public class SortRawTrainingImagesV2 {
    public static void main(String[] args) throws IOException {
        File sortedDir = new File("data/train-v2/raw-sorted");
        sortedDir.mkdirs();
        FindCardNumberFeatures cardFeatureCounter = new FindCardNumberFeatures();
        CardDetector cardDetector = new CardDetector(4, 66);
        for (File file : new File("data/train-v2/raw").listFiles((dir, name) -> name.matches(".*\\.jpg"))) {
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
            File targetDir = new File(sortedDir, Integer.toString(modalNumber));
            targetDir.mkdirs();
            Files.move(file.toPath(), new File(targetDir, file.getName()).toPath());
        }
    }
}
