package com.tom_e_white.set_game.preprocess;

import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.model.Card;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Takes a test image and a label file (ending in .txt) and creates a directory of test images with the
 * corresponding labels.
 */
public class CreateTestSetV2 {
    public static void main(String[] args) throws IOException {
        File testFile = new File("data/20170106_205743.jpg");
        CardDetector cardDetector = new CardDetector();
        List<CardImage> images = cardDetector.detect(testFile.getAbsolutePath(), false);
        List<Card> cards = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt")))
                .map(Card::new)
                .collect(Collectors.toList());
        File outDir = new File("data/test-v2");;
        outDir.mkdirs();
        for (int i = 0; i < images.size(); i++) {
            CardImage image = images.get(i);
            Card card = cards.get(i);
            File labelledDirectory = new File(outDir, card.getDescription().replace(" ", "-"));
            labelledDirectory.mkdirs();
            File newFile = new File(labelledDirectory, testFile.getName().replace(".jpg", "_" + (i + 1) + ".jpg"));
            UtilImageIO.saveImage(image.getImage(), newFile.getAbsolutePath());
        }

    }
}
