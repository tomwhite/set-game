package com.tom_e_white.set_game.preprocess;

import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.model.Card;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.tom_e_white.set_game.preprocess.TrainingDataV2.RAW_SORTED_DIRECTORY;

/**
 * Use {@link CardDetector} to create a training set of cards.
 */
public class CreateTrainingSetV2 {
    public static void main(String[] args) throws IOException {
        CardDetector cardDetector = new CardDetector(4, 66);
        File outDir = TrainingDataV2.RAW_LABELLED_DIRECTORY;
        outDir.mkdirs();
        for (File d : RAW_SORTED_DIRECTORY.listFiles((dir, name) -> name.matches("\\d"))) {
            for (File file : d.listFiles((dir, name) -> name.matches(".*\\.jpg"))) {
                System.out.println(file);
                List<CardImage> images = cardDetector.detect(file.getAbsolutePath(), false, true, 3, 9);
                int i = 0;
                for (CardImage image : images) {
                    String numberLabel = file.getParentFile().getName();
                    Card card = new Card(Integer.valueOf(numberLabel), i / 9, (i % 9) / 3, i % 3);
                    File labelledDirectory = new File(outDir, card.getDescription().replace(" ", "-"));
                    labelledDirectory.mkdirs();
                    File newFile = new File(labelledDirectory, file.getName().replace(".jpg", "_" + i + ".jpg"));
                    System.out.println(newFile);
                    UtilImageIO.saveImage(image.getImage(), newFile.getAbsolutePath());
                    i++;
                }
                break;
            }
        }
    }
}
