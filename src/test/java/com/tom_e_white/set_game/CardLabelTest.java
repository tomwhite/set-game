package com.tom_e_white.set_game;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class CardLabelTest {
    @Test
    public void testGetNumbersForLabel() {
        String label = "3 filled red squiggles";
        assertEquals(3, CardLabel.getNumber(label));
        assertEquals(1, CardLabel.getShadingNumber(label));
        assertEquals(1, CardLabel.getColourNumber(label));
        assertEquals(3, CardLabel.getShapeNumber(label));
    }

    @Test
    public void testGetNumbersForFile() {
        File file = new File("data/train-out/red-1-20170101_124752_6.jpg");
        assertEquals(1, CardLabel.getNumber(file));
        assertEquals(2, CardLabel.getShadingNumber(file));
        assertEquals(1, CardLabel.getColourNumber(file));
        assertEquals(3, CardLabel.getShapeNumber(file));
    }

    @Test
    public void testParseDescription() {
        String description = "3 solid red squiggles";
        assertEquals(Card.Number.THREE, Card.Number.parseDescription(description));
        assertEquals(Card.Shading.SOLID, Card.Shading.parseDescription(description));
        assertEquals(Card.Color.RED, Card.Color.parseDescription(description));
        assertEquals(Card.Shape.SQUIGGLE, Card.Shape.parseDescription(description));
    }

}
