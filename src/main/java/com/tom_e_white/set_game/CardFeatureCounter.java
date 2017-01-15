package com.tom_e_white.set_game;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

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

    GrayU8 gray = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
    GrayU8 median = ImageUtils.medianBlur(gray, 16);
    GrayU8 binary = ImageUtils.binarize(median, 0, 255);
    // remove small blobs through erosion and dilation
    GrayU8 eroded = BinaryImageOps.erode8(binary, 1, null);
    GrayU8 filtered = BinaryImageOps.dilate8(eroded, 1, null);

    GrayU8 edges = ImageUtils.findEdges(median);

    // Detect blobs inside the image using an 8-connect rule
    GrayS32 label = new GrayS32(gray.width,gray.height);
    List<Contour> contours = BinaryImageOps.contour(edges, ConnectRule.EIGHT, label);

    // Find polygons
    double splitFraction = 0.05;
    double minimumSideFraction = 0.1;
    List<List<PointIndex_I32>> externalContours = GeometryUtils.getExternalContours(contours, splitFraction, minimumSideFraction);
    List<List<PointIndex_I32>> internalContours = GeometryUtils.getInternalContours(contours, splitFraction, minimumSideFraction);

    if (debug) {

      // colors of contours
      int colorExternal = 0xFFFFFF;
      int colorInternal = 0xFF2020;

      // display the results
      BufferedImage visualBinary = VisualizeBinaryData.renderBinary(binary, false, null);
      BufferedImage visualFiltered = VisualizeBinaryData.renderBinary(filtered, false, null);
      BufferedImage visualLabel = VisualizeBinaryData.renderLabeledBG(label, contours.size(), null);
      BufferedImage visualContour = VisualizeBinaryData.renderContours(contours, colorExternal, colorInternal,
              gray.width, gray.height, null);
      BufferedImage visualEdges = VisualizeBinaryData.renderBinary(edges, false, null);

      // polygons
      BufferedImage polygon = new BufferedImage(gray.width, gray.height, BufferedImage.TYPE_INT_RGB);
      // Fit a polygon to each shape and draw the results
      Graphics2D g2 = polygon.createGraphics();
      g2.setStroke(new BasicStroke(2));

      for (List<PointIndex_I32> vertexes : externalContours) {
        g2.setColor(Color.RED);
        VisualizeShapes.drawPolygon(vertexes, true, g2);
      }
      for (List<PointIndex_I32> vertexes : internalContours) {
        // handle internal contours now
        g2.setColor(Color.BLUE);
        VisualizeShapes.drawPolygon(vertexes, true, g2);
      }

      ListDisplayPanel panel = new ListDisplayPanel();
      panel.addImage(image, "Original");
      panel.addImage(visualBinary, "Binary Original");
      panel.addImage(visualFiltered, "Binary Filtered");
      panel.addImage(visualLabel, "Labeled Blobs");
      panel.addImage(visualContour, "Contours");
      panel.addImage(visualEdges, "Edges");
      panel.addImage(polygon, "Binary Blob Contours");
      ShowImages.showWindow(panel, "Binary Operations", true);
    }
    return externalContours.size();

  }

  public static void main(String[] args) throws IOException {
    new CardFeatureCounter().scan(args[0], true);
  }
}
