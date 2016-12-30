import boofcv.abst.filter.blur.BlurFilter;
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
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
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

  public void scan(BufferedImage originalImage, boolean debug) throws IOException {
    // TODO

    ListDisplayPanel panel = new ListDisplayPanel();

    GrayU8 gray = ConvertBufferedImage.convertFromSingle(originalImage, null, GrayU8.class);
    GrayU8 blurred = gray.createSameShape();


    // size of the blur kernel. square region with a width of radius*2 + 1
    int radius = 8;

    // Apply gaussian blur using a procedural interface
    GBlurImageOps.gaussian(gray, blurred, -1, radius, null);
    panel.addImage(ConvertBufferedImage.convertTo(blurred, null, true),"Gaussian");

    // Apply a mean filter using an object oriented interface.  This has the advantage of automatically
    // recycling memory used in intermediate steps
    BlurFilter<GrayU8> filterMean = FactoryBlurFilter.mean(GrayU8.class, radius);
    filterMean.process(gray, blurred);
    panel.addImage(ConvertBufferedImage.convertTo(blurred, null, true),"Mean");

    // Apply a median filter using image type specific procedural interface.  Won't work if the type
    // isn't known at compile time
    GrayU8 median = BlurImageOps.median(gray, blurred, radius);
    panel.addImage(ConvertBufferedImage.convertTo(blurred, null, true),"Median");

    GrayU8 filtered = BinaryImageOps.erode8(gray, 1, null);
    filtered = BinaryImageOps.dilate8(filtered, 1, null);
    panel.addImage(ConvertBufferedImage.convertTo(filtered, null, true),"filtered");

    // Canny edge
    GrayU8 edgeImage = gray.createSameShape();
    CannyEdge<GrayU8,GrayS16> canny = FactoryEdgeDetectors.canny(2,true, true, GrayU8.class, GrayS16.class);
    // The edge image is actually an optional parameter.  If you don't need it just pass in null
    canny.process(median,0.1f,0.3f,edgeImage);

    // First get the contour created by canny
    List<EdgeContour> edgeContours = canny.getContours();
    // The 'edgeContours' is a tree graph that can be difficult to process.  An alternative is to extract
    // the contours from the binary image, which will produce a single loop for each connected cluster of pixels.
    // Note that you are only interested in external contours.
    List<Contour> contours = BinaryImageOps.contour(edgeImage, ConnectRule.EIGHT, null);

    BufferedImage visualBinary = VisualizeBinaryData.renderBinary(edgeImage, false, null);
    BufferedImage visualCannyContour = VisualizeBinaryData.renderContours(edgeContours,null,
            gray.width,gray.height,null);

    int colorExternal = 0xFFFFFF;
    int colorInternal = 0xFF2020;
    BufferedImage visualEdgeContour = VisualizeBinaryData.renderContours(contours, colorExternal, colorInternal,
            gray.width, gray.height, null);
    panel.addImage(visualBinary,"Binary Edges from Canny");
    panel.addImage(visualCannyContour, "Canny Trace Graph");
    panel.addImage(visualEdgeContour,"Contour from Canny Binary");

    ShowImages.showWindow(panel,"Image Blur Examples",true);

  }

  // Polynomial fitting tolerances
  static double splitFraction = 0.05;
  static double minimumSideFraction = 0.1;

  public void scan2(String filename) throws IOException {
    BufferedImage image = UtilImageIO.loadImage(filename);

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
    for( Contour c : contours ) {
      // Fit the polygon to the found external contour.  Note loop = true
      List<PointIndex_I32> vertexes = ShapeFittingOps.fitPolygon(c.external, true,
              splitFraction, minimumSideFraction, 100);

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

    ListDisplayPanel panel = new ListDisplayPanel();
    panel.addImage(visualBinary, "Binary Original");
    panel.addImage(visualFiltered, "Binary Filtered");
    panel.addImage(visualLabel, "Labeled Blobs");
    panel.addImage(visualContour, "Contours");
    panel.addImage(polygon, "Binary Blob Contours");
    ShowImages.showWindow(panel,"Binary Operations",true);

  }

  public static void main(String[] args) throws IOException {
    new CardDetector().scan2(args[0]);
  }
}
