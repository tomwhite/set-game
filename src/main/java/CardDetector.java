import boofcv.abst.filter.blur.BlurFilter;
import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.feature.detect.edge.EdgeContour;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.shapes.ShapeFittingOps;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.factory.filter.blur.FactoryBlurFilter;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.*;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Extract a standardised card (or cards) from an image.
 */
public class CardDetector {

  public void scan(String filename) throws IOException {
    scan(UtilImageIO.loadImage(filename));
  }

  public void scan(String filename, boolean debug) throws IOException {
    scan(UtilImageIO.loadImage(filename), debug);
  }

  public void scan(BufferedImage originalImage) throws IOException {
    scan(originalImage, false);
  }

  // Polynomial fitting tolerances
  static double splitFraction = 0.05;
  static double minimumSideFraction = 0.1;

  public void scan(BufferedImage image, boolean debug) throws IOException {
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

    // colors of contours
    int colorExternal = 0xFFFFFF;
    int colorInternal = 0xFF2020;

    // display the results
    BufferedImage visualBinary = VisualizeBinaryData.renderBinary(binary, false, null);
    BufferedImage visualFiltered = VisualizeBinaryData.renderBinary(filtered, false, null);
    BufferedImage visualLabel = VisualizeBinaryData.renderLabeledBG(label, contours.size(), null);
    BufferedImage visualContour = VisualizeBinaryData.renderContours(contours, colorExternal, colorInternal,
            input.width, input.height, null);

    // polygons
    BufferedImage polygon = new BufferedImage(input.width,input.height,BufferedImage.TYPE_INT_RGB);
    // Fit a polygon to each shape and draw the results
    Graphics2D g2 = polygon.createGraphics();
    g2.setStroke(new BasicStroke(2));

    List<PointIndex_I32> v = null;

    for( Contour c : contours ) {
      // Fit the polygon to the found external contour.  Note loop = true
      List<PointIndex_I32> vertexes = ShapeFittingOps.fitPolygon(c.external, true,
              splitFraction, minimumSideFraction, 100);
      if (v == null) {
        v = vertexes; // set first vertexes
      }

      g2.setColor(Color.RED);
      VisualizeShapes.drawPolygon(vertexes, true, g2);
      // TODO: what if polygon is not a quadrilateral

      // handle internal contours now
      g2.setColor(Color.BLUE);
      for( List<Point2D_I32> internal : c.internal ) {
        vertexes = ShapeFittingOps.fitPolygon(internal,true, splitFraction, minimumSideFraction,100);
        VisualizeShapes.drawPolygon(vertexes,true,g2);
      }
    }


    // Remove perspective distortion
    // see http://boofcv.org/index.php?title=Example_Remove_Perspective_Distortion
    Planar<GrayF32> input2 = ConvertBufferedImage.convertFromMulti(image, null, true, GrayF32.class);

    RemovePerspectiveDistortion<Planar<GrayF32>> removePerspective =
            new RemovePerspectiveDistortion<>(400, 500, ImageType.pl(3, GrayF32.class));

    // Specify the corners in the input image of the region.
    // Order matters! top-left, top-right, bottom-right, bottom-left
    // TODO: make sure order is correct (and not stretching card)
    Point2D_I32 p0 = v.get(0);
    Point2D_I32 p1 = v.get(1);
    Point2D_I32 p2 = v.get(2);
    Point2D_I32 p3 = v.get(3);
    System.out.println(p0);
    System.out.println(p1);
    System.out.println(p2);
    System.out.println(p3);
    if( !removePerspective.apply(input2,
            new Point2D_F64(p0.getX(), p0.getY()), new Point2D_F64(p1.getX(), p1.getY()),
            new Point2D_F64(p2.getX(), p2.getY()), new Point2D_F64(p3.getX(), p3.getY())) ){
      throw new RuntimeException("Failed!?!?");
    }

    Planar<GrayF32> output = removePerspective.getOutput();

    BufferedImage flat = ConvertBufferedImage.convertTo_F32(output,null,true);

    if (debug) {
      ListDisplayPanel panel = new ListDisplayPanel();
      panel.addImage(visualBinary, "Binary Original");
      panel.addImage(visualFiltered, "Binary Filtered");
      panel.addImage(visualLabel, "Labeled Blobs");
      panel.addImage(visualContour, "Contours");
      panel.addImage(polygon, "Binary Blob Contours");
      panel.addImage(flat,"Without Perspective Distortion");
      ShowImages.showWindow(panel, "Binary Operations", true);
    }

  }

  public static void main(String[] args) throws IOException {
    new CardDetector().scan(args[0], true);
  }
}
