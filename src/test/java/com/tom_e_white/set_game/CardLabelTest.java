package com.tom_e_white.set_game;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class CardLabelTest {
    @Test
    public void testGetColourNumber() {
        assertEquals(1, CardLabel.getColourNumber("3 filled red squiggles"));
        assertEquals(1, CardLabel.getColourNumber(new File("data/train-out/red-1-20170101_124752_6.jpg")));
    }
}
