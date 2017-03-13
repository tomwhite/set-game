package com.tom_e_white.set_game.preprocess;

import com.tom_e_white.set_game.preprocess.CardDetector;
import com.tom_e_white.set_game.preprocess.CardImage;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CardDetectorTest {

    private CardDetector cardDetector = new CardDetector();

    @Test
    public void testNoCards() throws IOException {
        List<CardImage> images = cardDetector.detect("data/egg.jpg");
        assertTrue(images.isEmpty());
    }

    @Test
    public void testSingleCard() throws IOException {
        List<CardImage> images = cardDetector.detect("data/one-card-20161230_192626.jpg", false, true);
        assertEquals(1, images.size());
    }

    @Test
    public void testErrantBorder() throws IOException {
        // this image has a border with some artifacts that may be detected as quadrilaterals
        List<CardImage> images = cardDetector.detect("data/purple-1-20170101_124559.jpg");
        assertEquals(9, images.size());
    }

    @Test
    public void testRotated() throws IOException {
        // this image is rotated through 90 degrees
        List<CardImage> images = cardDetector.detect("data/green-2-rotated-20161231_114543.jpg", false, true);
        assertEquals(9, images.size());
    }

    @Test
    public void testTrainingImagesContainNineCards() {
        Arrays.stream(new File("data/train").listFiles((dir, name) -> name.matches(".*\\.jpg"))).forEach(
                file -> {
                    try {
                        System.out.println(file);
                        List<CardImage> images = cardDetector.detect(file.getAbsolutePath());
                        assertEquals(9, images.size());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
