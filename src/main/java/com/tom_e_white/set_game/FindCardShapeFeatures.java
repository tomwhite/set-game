package com.tom_e_white.set_game;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.image.GeometryUtils;
import com.tom_e_white.set_game.image.ImageProcessingPipeline;
import com.tom_e_white.set_game.image.Shape;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Find features for shapes on a card.
 */
public class FindCardShapeFeatures implements FeatureFinder {

  @Override
  public int getLabel(String filename) {
    return CardLabel.getShapeNumber(new File(filename));
  }

  @Override
  public String getSummaryLine(String filename, double[] features) {
    StringBuilder sb = new StringBuilder(getLabel(filename));
    for (double f : features) {
      sb.append(",").append(f);
    }
    return sb.toString();
  }

  @Override
  public LabelledVector getLabelledVector(String summaryLine) {
    String[] split = summaryLine.split(",");
    double[] vector = new double[split.length - 1];
    for (int i = 0; i < split.length - 1; i++) {
      vector[i] = Double.parseDouble(split[i + 1]);
    }
    return new LabelledVector(Integer.parseInt(split[0]), vector);
  }

  @Override
  public double[] find(BufferedImage image, boolean debug) throws IOException {
    // Based on code from http://boofcv.org/index.php?title=Example_Binary_Image

    ListDisplayPanel panel = debug ? new ListDisplayPanel() : null;

    List<Shape> shapes = ImageProcessingPipeline.fromBufferedImage(image, panel)
            .gray()
            .medianBlur(3) // this is fairly critical
            .edges()
            .dilate()
            .contours()
            .polygons(0.05, 0.05)
            .getExternalShapes();

    int expectedWidth = 40 * 3; // 40mm
    int expectedHeight = 20 * 3; // 20mm
    int tolerancePct = 40;
    List<Shape> filtered = GeometryUtils.filterByArea(shapes, expectedWidth, expectedHeight, tolerancePct);
    List<Shape> nonOverlapping = GeometryUtils.filterNonOverlappingBoundingBoxes(filtered);

    Optional<double[]> cardShapeFeatures = nonOverlapping.stream()
            .map(Shape::getPolygon)
            .map(p -> new double[] { p.size(), p.isConvex() ? 1 : 0 })
            .findFirst();

    if (debug) {
      ShowImages.showWindow(panel, getClass().getSimpleName(), true);
    }

    return cardShapeFeatures.orElse(null); // improve
  }

  @Override
  public String getFileName() {
    return "shape.csv";
  }

  public static void main(String[] args) throws IOException {
    BufferedImage image = UtilImageIO.loadImage(args[0]);
    FindCardShapeFeatures featureFinder = new FindCardShapeFeatures();
    double[] features = featureFinder.find(image, true);
    System.out.println(featureFinder.getLabel(args[0]));
    System.out.println(Arrays.toString(features));
  }
}
