package com.tom_e_white.set_game;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class PredictCardNumberOnTrainingDataTest {
    @Test
    public void test() throws IOException {
        double accuracy = PredictCardNumberOnTrainingData.computeAccuracyOnTrainingData();
        assertTrue(accuracy > 99);
    }
}
