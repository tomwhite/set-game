package com.tom_e_white.set_game.preprocess;

import georegression.struct.shapes.Quadrilateral_F64;

import java.awt.image.BufferedImage;

/**
 * The image of an individual card, along with some geometric information.
 */
public class CardImage {
    private final BufferedImage image;
    private final Quadrilateral_F64 externalQuadrilateral;

    public CardImage(BufferedImage image, Quadrilateral_F64 externalQuadrilateral) {
        this.image = image;
        this.externalQuadrilateral = externalQuadrilateral;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Quadrilateral_F64 getExternalQuadrilateral() {
        return externalQuadrilateral;
    }
}
