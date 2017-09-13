package com.tom_e_white.set_game.predict;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;

public class PredictCardsOnTestDataTest {

    public static final File TEST_LAYOUT = new File("data/20170106_205743.jpg");

    @Test
    public void testV1() throws IOException, ParseException {
        double accuracy = PredictCardsOnTestData.predict(TEST_LAYOUT, new CardPredictor());
        assertEquals(60, accuracy, 1);
    }

    @Test
    public void testV2() throws IOException, ParseException {
        double accuracy = PredictCardsOnTestData.predict(TEST_LAYOUT, new CardPredictorV2());
        assertEquals(73, accuracy, 1);
    }

    @Test
    public void testConvNet() throws IOException, ParseException {
        double accuracy = PredictCardsOnTestData.predict(TEST_LAYOUT, new CardPredictorConvNet());
        assertEquals(93, accuracy, 1);
    }

    @Test
    public void testConvNetPerAttribute() throws IOException, ParseException {
        double accuracy = PredictCardsOnTestData.predict(TEST_LAYOUT, new CardPredictorConvNetPerAttribute());
        assertEquals(100, accuracy, 1);
    }
}
