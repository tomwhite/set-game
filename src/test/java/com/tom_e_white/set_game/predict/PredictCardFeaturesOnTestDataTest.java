package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.predict.PredictCardFeaturesOnTestData;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertArrayEquals;

public class PredictCardFeaturesOnTestDataTest {
    @Test
    public void test() throws IOException, ParseException {
        double[] accuracies = PredictCardFeaturesOnTestData.predict(new File("data/20170106_205743.jpg"));
        assertArrayEquals(new double[] { 100, 66, 93, 100 }, accuracies, 1);
    }
}
