package com.tom_e_white.set_game.preprocess;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ImageGridPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.model.Card;
import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.tom_e_white.set_game.preprocess.TrainingDataV2.RAW_LABELLED_DIRECTORY;
import static com.tom_e_white.set_game.preprocess.TrainingDataV2.RAW_SORTED_DIRECTORY;

/**
 * Use {@link CardDetector} to create a training set of cards.
 */
public class CreateTrainingSetV2 {
    public static void main(String[] args) throws IOException {
        if (!RAW_LABELLED_DIRECTORY.exists()) {
            CardDetector cardDetector = new CardDetector(4, 66);
            File outDir = RAW_LABELLED_DIRECTORY;
            outDir.mkdirs();
            for (File d : RAW_SORTED_DIRECTORY.listFiles((dir, name) -> name.matches("\\d"))) {
                int numberLabel = Integer.valueOf(d.getName());
                for (File file : d.listFiles((dir, name) -> name.matches(".*\\.jpg"))) {
                    System.out.println(file);
                    List<CardImage> images = cardDetector.detect(file.getAbsolutePath(), false, true, 3, 9);
                    int i = 0;
                    for (CardImage image : images) {
                        Card card = new Card(numberLabel, (i % 9) / 3, i / 9, i % 3);
                        File labelledDirectory = new File(outDir, card.getDescription().replace(" ", "-"));
                        labelledDirectory.mkdirs();
                        File newFile = new File(labelledDirectory, file.getName().replace(".jpg", "_" + numberLabel + "_" + i + ".jpg"));
                        UtilImageIO.saveImage(image.getImage(), newFile.getAbsolutePath());
                        i++;
                    }
                }
            }
        }

        ViewLabelledImagesV2.view(RAW_LABELLED_DIRECTORY);
    }

}
