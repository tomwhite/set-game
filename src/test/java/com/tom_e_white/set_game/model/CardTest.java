package com.tom_e_white.set_game.model;

import com.tom_e_white.set_game.model.Card;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class CardTest {
    @Test
    public void testParseDescription() {
        String description = "3 red solid squiggles";
        assertEquals(Card.Number.THREE, Card.Number.parseDescription(description));
        assertEquals(Card.Color.RED, Card.Color.parseDescription(description));
        assertEquals(Card.Shading.SOLID, Card.Shading.parseDescription(description));
        assertEquals(Card.Shape.SQUIGGLE, Card.Shape.parseDescription(description));
    }

    @Test
    public void testParseFilename() {
        File file = new File("data/train-out/red-1-20170101_124752_6.jpg");
        assertEquals(Card.Number.ONE, Card.Number.parseFilename(file));
        assertEquals(Card.Shading.STRIPED, Card.Shading.parseFilename(file));
        assertEquals(Card.Color.RED, Card.Color.parseFilename(file));
        assertEquals(Card.Shape.SQUIGGLE, Card.Shape.parseFilename(file));
    }
}
