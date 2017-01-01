package com.tom_e_white.set_game;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.shapes.ShapeFittingOps;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.*;
import georegression.metric.Area2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.struct.shapes.Quadrilateral_F64;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Extract a standardised card (or cards) from an image.
 */
public class CardDetector {

  public List<BufferedImage> scan(String filename) throws IOException {
    return scan(UtilImageIO.loadImage(filename));
  }

  public List<BufferedImage> scan(String filename, boolean debug) throws IOException {
    return scan(UtilImageIO.loadImage(filename), debug);
  }

  public List<BufferedImage> scan(BufferedImage originalImage) throws IOException {
    return scan(originalImage, false);
  }

  // Polynomial fitting tolerances
  static double splitFraction = 0.05;
  static double minimumSideFraction = 0.1;

  public List<BufferedImage> scan(BufferedImage image, boolean debug) throws IOException {
    // from http://boofcv.org/index.php?title=Example_Binary_Image

    GrayU8 gray = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
    GrayU8 blurred = gray.createSameShape();


    // size of the blur kernel. square region with a width of radius*2 + 1
    int radius = 16;
    GrayU8 median = BlurImageOps.median(gray, blurred, radius);
    //panel.addImage(ConvertBufferedImage.convertTo(blurred, null, true),"Median");

    // convert into a usable format
    GrayU8 input = median;
    GrayU8 binary = new GrayU8(input.width,input.height);
    GrayS32 label = new GrayS32(input.width,input.height);

    // Select a global threshold using Otsu's method.
    double threshold = GThresholdImageOps.computeOtsu(input, 0, 255);

    // Apply the threshold to create a binary image
    ThresholdImageOps.threshold(input, binary, (int) threshold, false); // "down=false" to find exterior contours

    // remove small blobs through erosion and dilation
    // The null in the input indicates that it should internally declare the work image it needs
    // this is less efficient, but easier to code.
    GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
    filtered = BinaryImageOps.dilate8(filtered, 1, null);

    // Detect blobs inside the image using an 8-connect rule
    List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label);

    Graphics2D g2 = null;
    // polygons
    BufferedImage polygon = new BufferedImage(input.width, input.height, BufferedImage.TYPE_INT_RGB);
    if (debug) {
      // Fit a polygon to each shape and draw the results
      g2 = polygon.createGraphics();
      g2.setStroke(new BasicStroke(2));
    }

    List<BufferedImage> cardImages = new ArrayList<>();
    List<List<PointIndex_I32>> quads = new ArrayList<>();
    for( Contour c : contours ) {
      // Fit the polygon to the found external contour.  Note loop = true
      List<PointIndex_I32> vertexes = ShapeFittingOps.fitPolygon(c.external, true,
              splitFraction, minimumSideFraction, 100);

      if (GeometryUtils.isQuadrilateral(vertexes)) {
        quads.add(vertexes);
      }

      if (debug) {
        g2.setColor(Color.RED);
        VisualizeShapes.drawPolygon(vertexes, true, g2);

        // handle internal contours now
        g2.setColor(Color.BLUE);
        for (List<Point2D_I32> internal : c.internal) {
          vertexes = ShapeFittingOps.fitPolygon(internal, true, splitFraction, minimumSideFraction, 100);
          VisualizeShapes.drawPolygon(vertexes, true, g2);
        }
      }
    }

    // Only include shapes that are within given percentage of the mean area. This filters out image artifacts that
    // happen to be quadrilaterals that are not cards (since they are usually a different size).
    int areaTolerancePct = 20;
    List<Quadrilateral_F64> quadrilaterals = quads.stream().map(GeometryUtils::toQuadrilateral).collect(Collectors.toList());
    OptionalDouble meanArea = quadrilaterals.stream().mapToDouble(Area2D_F64::quadrilateral).average();
    quadrilaterals.stream().filter(q -> {
      double m = meanArea.getAsDouble();
      return Math.abs(Area2D_F64.quadrilateral(q) - m) / m <= areaTolerancePct / 100.0;
    }).forEach(q -> {
        // Remove perspective distortion
        Planar<GrayF32> output = GeometryUtils.removePerspectiveDistortion(image, q, 57 * 3, 89 * 3); // actual cards measure 57 mm x 89 mm
        BufferedImage flat = ConvertBufferedImage.convertTo_F32(output, null, true);
        cardImages.add(flat);
        // UtilImageIO.saveImage(flat, "/tmp/flat.png");
      }
    );

    if (debug) {

      // colors of contours
      int colorExternal = 0xFFFFFF;
      int colorInternal = 0xFF2020;

      // display the results
      BufferedImage visualBinary = VisualizeBinaryData.renderBinary(binary, false, null);
      BufferedImage visualFiltered = VisualizeBinaryData.renderBinary(filtered, false, null);
      BufferedImage visualLabel = VisualizeBinaryData.renderLabeledBG(label, contours.size(), null);
      BufferedImage visualContour = VisualizeBinaryData.renderContours(contours, colorExternal, colorInternal,
              input.width, input.height, null);


      ListDisplayPanel panel = new ListDisplayPanel();
      panel.addImage(image, "Original");
      panel.addImage(visualBinary, "Binary Original");
      panel.addImage(visualFiltered, "Binary Filtered");
      panel.addImage(visualLabel, "Labeled Blobs");
      panel.addImage(visualContour, "Contours");
      panel.addImage(polygon, "Binary Blob Contours");
      int i = 1;
      for (BufferedImage card : cardImages) {
        panel.addImage(card, "Card " + i++);
      }
      ShowImages.showWindow(panel, "Binary Operations", true);
    }

    return cardImages;

  }

  public static void main(String[] args) throws IOException {
    new CardDetector().scan(args[0], true);
  }
}
