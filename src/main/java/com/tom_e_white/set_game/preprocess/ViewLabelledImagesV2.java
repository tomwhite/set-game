package com.tom_e_white.set_game.preprocess;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ImageGridPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.tom_e_white.set_game.preprocess.TrainingDataV2.LABELLED_DIRECTORY;

public class ViewLabelledImagesV2 {
    public static void view(File directory) {
        ListDisplayPanel panel = new ListDisplayPanel();
        int numCols = 20;

        int numImagesPerLabel = -1;
        char prevNumLabel = ' ';
        for (File d : directory.listFiles((dir, name) -> !name.matches("\\..*"))) {
            String label = d.getName();
            BufferedImage[] bufferedImages = Arrays.stream(d.listFiles((dir, name) -> name.matches(".*\\.jpg")))
                    .map(f -> UtilImageIO.loadImage(f.getAbsolutePath()))
                    .map(bi -> resize(bi, bi.getWidth() / 3, bi.getHeight() / 3))
                    .collect(Collectors.toList())
                    .toArray(new BufferedImage[0]);
            panel.addItem(new ImageGridPanel((bufferedImages.length / numCols) + 1, numCols, bufferedImages), label);
            System.out.println(label + "\t" + bufferedImages.length);
            if (prevNumLabel != label.charAt(0)) {
                numImagesPerLabel = bufferedImages.length;
                prevNumLabel = label.charAt(0);
            } else if (numImagesPerLabel != bufferedImages.length) {
                throw new IllegalStateException("Expected " + numImagesPerLabel + " images, but only found " + bufferedImages.length + " for " + label);
            }
        }

        ShowImages.showWindow(panel, CreateTrainingSetV2.class.getSimpleName(), true);
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        try {
            return Thumbnails.of(img).size(newW, newH).asBufferedImage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        view(LABELLED_DIRECTORY);
    }
}
