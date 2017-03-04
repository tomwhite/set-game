package com.tom_e_white.set_game;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.image.GeometryUtils;
import com.tom_e_white.set_game.image.ImageProcessingPipeline;
import com.tom_e_white.set_game.image.Shape;
import smile.classification.Classifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Count the number of shapes on a card.
 */
public class FindCardNumberFeatures extends FeatureFinder {

  @Override
  public int getLabel(String filename) {
    return Card.Number.parseFilename(new File(filename)).ordinal();
  }

  @Override
  public int getLabelNumberFromLabel(String label) {
    return Card.Number.parseDescription(label).ordinal();
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

    if (debug) {
      System.out.println(shapes);
      System.out.println(filtered);
      System.out.println(nonOverlapping);
      ShowImages.showWindow(panel, "Binary Operations", true);
    }
    return new double[] { nonOverlapping.size() };
  }

  @Override
  public String getFileSuffix() {
    return "number.csv";
  }

  @Override
  public Classifier<double[]> getClassifier() throws IOException, ParseException {
    return doubles -> ((int) doubles[0]) % 3; // just return as an int mod 3
  }

  public static void main(String[] args) throws IOException {
    BufferedImage image = UtilImageIO.loadImage(args[0]);
    FindCardNumberFeatures featureFinder = new FindCardNumberFeatures();
    double[] features = featureFinder.find(image, true);
    System.out.println(featureFinder.getSummaryLine(args[0], features));
  }

}
