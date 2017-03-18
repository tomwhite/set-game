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
        if (!(args.length > 0 && args[0].equals("-viewOnly"))) {
            CardDetector cardDetector = new CardDetector(4, 66);
            File outDir = RAW_LABELLED_DIRECTORY;
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
                }
            }
        }

        ListDisplayPanel panel = new ListDisplayPanel();
        int numCols = 15;

        for (File d : RAW_LABELLED_DIRECTORY.listFiles((dir, name) -> !name.matches("\\..*"))) {
            String label = d.getName();
            BufferedImage[] bufferedImages = Arrays.stream(d.listFiles((dir, name) -> name.matches(".*\\.jpg")))
                    .map(f -> UtilImageIO.loadImage(f.getAbsolutePath()))
                    .map(bi -> resize(bi, bi.getWidth() / 3, bi.getHeight() / 3))
                    .collect(Collectors.toList())
                    .toArray(new BufferedImage[0]);
            panel.addItem(new ImageGridPanel((bufferedImages.length / numCols) + 1, numCols, bufferedImages), label);
        }

        ShowImages.showWindow(panel, CreateTrainingSetV2.class.getSimpleName(), true);
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        try {
            return Thumbnails.of(img).size(newW, newH).asBufferedImage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
