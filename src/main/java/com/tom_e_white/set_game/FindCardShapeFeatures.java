package com.tom_e_white.set_game;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.image.ImageProcessingPipeline;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

/**
 * Find features for shapes on a card.
 */
public class FindCardShapeFeatures {

  public static class CardShapeFeatures {
    private final int numSides;
    private final boolean isConvex;

    public CardShapeFeatures(int numSides, boolean isConvex) {
      this.numSides = numSides;
      this.isConvex = isConvex;
    }

    public int getNumSides() {
      return numSides;
    }

    public boolean isConvex() {
      return isConvex;
    }

    @Override
    public String toString() {
      return "CardShapeFeatures{" +
              "numSides=" + numSides +
              ", isConvex=" + isConvex +
              '}';
    }
  }

  public CardShapeFeatures scan(String filename) throws IOException {
    return scan(UtilImageIO.loadImage(filename));
  }

  private CardShapeFeatures scan(String filename, boolean debug) throws IOException {
    return scan(UtilImageIO.loadImage(filename), debug);
  }

  private CardShapeFeatures scan(BufferedImage originalImage) throws IOException {
    return scan(originalImage, false);
  }

  private CardShapeFeatures scan(BufferedImage image, boolean debug) throws IOException {
    // Based on code from http://boofcv.org/index.php?title=Example_Binary_Image

    ListDisplayPanel panel = debug ? new ListDisplayPanel() : null;

    // TODO: Use shapes found by CardFeatureCounter, since these are "good" contours. Maybe join?
    Optional<CardShapeFeatures> cardShapeFeatures = ImageProcessingPipeline.fromBufferedImage(image, panel)
            .gray()
            .medianBlur(3) // this is fairly critical
            .edges()
            .dilate()
            .contours()
            .polygons(0.05, 0.05)
            .getExternalPolygons()
            .stream()
            .map(p -> new CardShapeFeatures(p.size(), p.isConvex()))
            .findFirst();

    if (debug) {
      ShowImages.showWindow(panel, getClass().getSimpleName(), true);
    }

    return cardShapeFeatures.orElse(null); // improve
  }

  public static void main(String[] args) throws IOException {
    CardShapeFeatures cardShapeFeatures = new FindCardShapeFeatures().scan(args[0], true);
    System.out.println(cardShapeFeatures);
  }
}
