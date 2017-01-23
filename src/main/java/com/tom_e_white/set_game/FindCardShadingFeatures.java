package com.tom_e_white.set_game;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;
import com.tom_e_white.set_game.image.GeometryUtils;
import com.tom_e_white.set_game.image.ImageProcessingPipeline;
import com.tom_e_white.set_game.image.Shape;
import georegression.struct.shapes.Quadrilateral_F64;
import georegression.struct.shapes.RectangleLength2D_F32;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Find features for shading on a card.
 */
public class FindCardShadingFeatures implements FeatureFinder<FindCardShadingFeatures.CardShadingFeatures> {

  public static class CardShadingFeatures implements Features {
    private final int shapeShadingLabel;
    private final int numEdgeExternalContours;
    private final int numEdgeInternalContours;
    private final int numBinaryExternalContours;
    private final int numBinaryInternalContours;

    public CardShadingFeatures(int shapeShadingLabel, int numEdgeExternalContours, int numEdgeInternalContours,
                               int numBinaryExternalContours, int numBinaryInternalContours) {
      this.shapeShadingLabel = shapeShadingLabel;
      this.numEdgeExternalContours = numEdgeExternalContours;
      this.numEdgeInternalContours = numEdgeInternalContours;
      this.numBinaryExternalContours = numBinaryExternalContours;
      this.numBinaryInternalContours = numBinaryInternalContours;
    }

    @Override
    public String getSummaryLine() {
      return shapeShadingLabel + "," +
              numEdgeExternalContours + "," + numEdgeInternalContours + "," +
              numBinaryExternalContours + "," + numBinaryInternalContours;
    }

    @Override
    public String toString() {
      return "CardShadingFeatures{" +
              "shapeShadingLabel=" + shapeShadingLabel +
              ", numEdgeExternalContours=" + numEdgeExternalContours +
              ", numEdgeInternalContours=" + numEdgeInternalContours +
              ", numBinaryExternalContours=" + numBinaryExternalContours +
              ", numBinaryInternalContours=" + numBinaryInternalContours +
              '}';
    }
  }

  @Override
  public CardShadingFeatures find(String filename, boolean debug) throws IOException {
    // Based on code from http://boofcv.org/index.php?title=Example_Binary_Image

    BufferedImage image = UtilImageIO.loadImage(filename);

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

    Optional<RectangleLength2D_F32> box = nonOverlapping.stream()
            .map(Shape::getBoundingBox)
            .findFirst();

    CardShadingFeatures features = null;
    if (box.isPresent()) {
      Quadrilateral_F64 quad = new Quadrilateral_F64();
      RectangleLength2D_F32 rect = box.get();
      convert(rect, quad);
      Planar<GrayF32> output = GeometryUtils.removePerspectiveDistortion(image, quad, 200, 100);
      BufferedImage shape = ConvertBufferedImage.convertTo_F32(output, null, true);
      if (debug) {
        panel.addImage(shape, "Shape");
      }

      ImageProcessingPipeline.ContourPolygonsProcessor edgeProcessor = ImageProcessingPipeline.fromBufferedImage(shape, panel)
              .gray()
              .edges()
              .contours()
              .polygons(0.01, 0.01);

      ImageProcessingPipeline.ContourPolygonsProcessor binaryProcessor = ImageProcessingPipeline.fromBufferedImage(shape, panel)
              .gray()
              .binarize(0, 255, true)
              .contours()
              .polygons(0.01, 0.01);

      features = new CardShadingFeatures(CardLabel.getShadingNumber(new File(filename)),
              edgeProcessor.getExternalContours().size(), edgeProcessor.getInternalContours().size(),
              binaryProcessor.getExternalContours().size(), binaryProcessor.getInternalContours().size()
      );
    }

    if (debug) {
      ShowImages.showWindow(panel, getClass().getSimpleName(), true);
    }

    return features;
  }

  @Override
  public String getFileName() {
    return "shading.csv";
  }

  private static void convert(RectangleLength2D_F32 input, Quadrilateral_F64 output) {
    output.a.x = input.x0;
    output.a.y = input.y0;
    output.b.x = (input.x0 + input.width);
    output.b.y = input.y0;
    output.c.x = (input.x0 + input.width);
    output.c.y = (input.y0 + input.height);
    output.d.x = input.x0;
    output.d.y = (input.y0 + input.height);
  }

  public static void main(String[] args) throws IOException {
    CardShadingFeatures cardShadingFeatures = new FindCardShadingFeatures().find(args[0], true);
    System.out.println(cardShadingFeatures);
  }
}
