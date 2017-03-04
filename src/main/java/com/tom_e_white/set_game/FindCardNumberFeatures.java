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
import java.util.Arrays;
import java.util.List;

/**
 * Count the number of shapes on a card.
 */
public class FindCardNumberFeatures implements FeatureFinder {

  @Override
  public int getLabel(String filename) {
    return CardLabel.getNumber(new File(filename));
  }

  @Override
  public int getLabelNumberFromLabel(String label) {
    return CardLabel.getNumber(label);
  }

  @Override
  public String getSummaryLine(int label, double[] features) {
    StringBuilder sb = new StringBuilder().append(label);
    for (double f : features) {
      sb.append(",").append(f);
    }
    return sb.toString();
  }

  @Override
  public String getSummaryLine(String filename, double[] features) {
    return getSummaryLine(getLabel(filename), features);
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
  public String getFileName() {
    return "number.csv";
  }

  @Override
  public Classifier<double[]> getClassifier() throws IOException, ParseException {
    return doubles -> {
      return (int) doubles[0] - 1; // TODO: remove need to subtract one
    };
  }

  public static void main(String[] args) throws IOException {
    BufferedImage image = UtilImageIO.loadImage(args[0]);
    FindCardNumberFeatures featureFinder = new FindCardNumberFeatures();
    double[] features = featureFinder.find(image, true);
    System.out.println(featureFinder.getLabel(args[0]));
    System.out.println(Arrays.toString(features));
  }

}
