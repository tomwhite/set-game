package com.tom_e_white.set_game.preprocess;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;
import com.tom_e_white.set_game.image.GeometryUtils;
import com.tom_e_white.set_game.image.ImageProcessingPipeline;
import georegression.struct.shapes.Quadrilateral_F64;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Extract a standardised card (or cards) from an image.
 */
public class CardDetector {

  private final int medianBlur;
  private final int areaTolerancePct;

  public CardDetector() {
    this(16, 25);
  }

  public CardDetector(int medianBlur, int areaTolerancePct) {
    this.medianBlur = medianBlur;
    this.areaTolerancePct = areaTolerancePct;
  }

  public List<CardImage> detect(String filename) throws IOException {
    return detect(filename, false);
  }

  public List<CardImage> detect(String filename, boolean debug) throws IOException {
    return detect(filename, debug, false);
  }

  public List<CardImage> detect(String filename, boolean debug, boolean allowRotated) throws IOException {
    return detect(filename, debug, allowRotated, -1, -1);
  }

  public List<CardImage> detect(String filename, boolean debug, boolean allowRotated, int expectedRows, int expectedColumns) throws IOException {
    return detect(UtilImageIO.loadImage(filename), filename, debug, allowRotated, expectedRows, expectedColumns);
  }

  public List<CardImage> detect(BufferedImage image, String filename, boolean debug, boolean allowRotated) throws IOException {
    return detect(image, filename, debug, allowRotated, -1, -1);
  }

  public List<CardImage> detect(BufferedImage image, String filename, boolean debug, boolean allowRotated, int expectedRows, int expectedColumns) throws IOException {
    // Based on code from http://boofcv.org/index.php?title=Example_Binary_Image

    String imageInfo = filename == null ? image.toString() : filename;

    if (!allowRotated && image.getWidth() > image.getHeight()) {
      throw new IllegalArgumentException("Image height must be greater than width: " + imageInfo);
    }

    ListDisplayPanel panel = debug ? new ListDisplayPanel() : null;

    List<Quadrilateral_F64> quads = ImageProcessingPipeline.fromBufferedImage(image, panel)
            .gray()
            .medianBlur(medianBlur)
            .binarize(0, 255)
            .erode()
            .dilate()
            .contours()
            .polygons(0.05, 0.1)
            .getExternalQuadrilaterals();

    // Only include shapes that are within given percentage of the mean area. This filters out image artifacts that
    // happen to be quadrilaterals that are not cards (since they are usually a different size).
    List<Quadrilateral_F64> cards = GeometryUtils.filterByArea(quads, areaTolerancePct);
    List<List<Quadrilateral_F64>> rows = GeometryUtils.sortRowWise(cards);// sort into a stable order
    if (expectedRows != -1 && rows.size() != expectedRows) {
      throw new IllegalArgumentException(String.format("Expected %s rows, but detected %s: %s", expectedRows, rows.size(), imageInfo));
    }
    if (expectedColumns != -1) {
      rows.forEach(row -> {
        if (row.size() != expectedColumns) {
          throw new IllegalArgumentException(String.format("Expected %s columns, but detected %s: %s", expectedColumns, row.size(), imageInfo));
        }
      });
    }
    List<CardImage> cardImages = rows.stream()
            .flatMap(List::stream)
            .map(q -> {
              // Remove perspective distortion
              Planar<GrayF32> output = GeometryUtils.removePerspectiveDistortion(image, q, 57 * 3, 89 * 3); // actual cards measure 57 mm x 89 mm
              BufferedImage im = ConvertBufferedImage.convertTo_F32(output, null, true);
              return new CardImage(im, q);
            }
    ).collect(Collectors.toList());

    if (debug) {
      int i = 1;
      for (CardImage card : cardImages) {
        panel.addImage(card.getImage(), "Card " + i++);
      }
      ShowImages.showWindow(panel, getClass().getSimpleName(), true);
    }

    return cardImages;

  }

  public static void main(String[] args) throws IOException {
    new CardDetector().detect(args[0], true, true);
  }
}
