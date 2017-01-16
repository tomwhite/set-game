package com.tom_e_white.set_game;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import georegression.metric.Intersection2D_F32;
import georegression.struct.shapes.RectangleLength2D_F32;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Count the number of shapes on a card.
 */
public class CardFeatureCounter {

  public int scan(String filename) throws IOException {
    return scan(UtilImageIO.loadImage(filename));
  }

  public int scan(String filename, boolean debug) throws IOException {
    return scan(UtilImageIO.loadImage(filename), debug);
  }

  public int scan(BufferedImage originalImage) throws IOException {
    return scan(originalImage, false);
  }

  public int scan(BufferedImage image, boolean debug) throws IOException {
    // Based on code from http://boofcv.org/index.php?title=Example_Binary_Image

    ListDisplayPanel panel = debug ? new ListDisplayPanel() : null;

    List<RectangleLength2D_F32> boxes = ImageProcessingPipeline.fromBufferedImage(image, panel)
            .gray()
            .medianBlur(3) // this is fairly critical
            .edges()
            .dilate()
            .contours()
            .polygons(0.05, 0.05)
            .getExternalBoundingBoxes();

    int expectedWidth = 40 * 3; // 40mm
    int expectedHeight = 20 * 3; // 20mm
    int tolerancePct = 40;
    List<RectangleLength2D_F32> shapes = boxes.stream()
            .filter(b -> {
//              System.out.println(Math.abs(b.getWidth() - expectedWidth) / expectedWidth);
//              System.out.println(Math.abs(b.getHeight() - expectedHeight) / expectedHeight);
              return Math.abs(b.getWidth() - expectedWidth) / expectedWidth <= tolerancePct / 100.0
                      && Math.abs(b.getHeight() - expectedHeight) / expectedHeight <= tolerancePct / 100.0;
            })
            .collect(Collectors.toList());
    List<RectangleLength2D_F32> nonOverlapping = new ArrayList<>(shapes);
    for (int i = 0; i < shapes.size(); i++) {
      for (int j = 0; j < i; j++) {
        RectangleLength2D_F32 rect1 = shapes.get(i);
        RectangleLength2D_F32 rect2 = shapes.get(j);
        if (Intersection2D_F32.intersection(rect1, rect2) != null) {
          nonOverlapping.remove(rect2);
        }
      }
    }

    if (debug) {
      System.out.println(boxes);
      System.out.println(shapes);
      System.out.println(nonOverlapping);
      ShowImages.showWindow(panel, "Binary Operations", true);
    }
    return nonOverlapping.size();

  }

  public static void main(String[] args) throws IOException {
    new CardFeatureCounter().scan(args[0], true);
  }
}
