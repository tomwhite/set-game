package com.tom_e_white.set_game.predict;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PredictSetsOnTestDataTest {

    public static final List<File> TEST_FILES = Arrays.asList(
        new File("data/20170106_205743.jpg"),
        new File("data/nine-cards-mixed-20161230_192740.jpg"),
        new File("data/webcam/20170529_184024.png"),
        new File("data/webcam/20170529_203823.png"),
        new File("data/webcam/20170604_134005.png"),
        new File("data/webcam/20170604_134234.png")
    );

    @Test
    public void testV1() throws IOException, ParseException {
        double accuracy = PredictSetsOnTestData.predict(TEST_FILES, new CardPredictor());
        assertEquals(33, accuracy, 1);
    }

    @Test
    public void testV2() throws IOException, ParseException {
        double accuracy = PredictSetsOnTestData.predict(TEST_FILES, new CardPredictorV2());
        assertEquals(33, accuracy, 1);
    }

    @Test
    public void testConvNet() throws IOException, ParseException {
        double accuracy = PredictSetsOnTestData.predict(TEST_FILES, new CardPredictorConvNet());
        assertEquals(100, accuracy, 1);
    }

    @Test
    public void testConvNetPerAttribute() throws IOException, ParseException {
        double accuracy = PredictSetsOnTestData.predict(TEST_FILES, new CardPredictorConvNetPerAttribute());
        assertEquals(100, accuracy, 1);
    }
}
