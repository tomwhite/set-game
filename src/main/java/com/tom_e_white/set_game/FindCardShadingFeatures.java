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
import georegression.struct.shapes.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Find features for shading on a card.
 */
public class FindCardShadingFeatures implements FeatureFinder<FindCardShadingFeatures.CardShadingFeatures> {

  public static class CardShadingFeatures implements Features {
    private final int shapeShadingLabel;

    public CardShadingFeatures(int shapeShadingLabel) {
      this.shapeShadingLabel = shapeShadingLabel;
    }

    @Override
    public String getSummaryLine() {
      return shapeShadingLabel + "";
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

    if (debug) {
      if (box.isPresent()) {
        Quadrilateral_F64 quad = new Quadrilateral_F64();
        RectangleLength2D_F32 rect = box.get();
        convert(rect, quad);
        Planar<GrayF32> output = GeometryUtils.removePerspectiveDistortion(image, quad, 100, 50);
        BufferedImage shape = ConvertBufferedImage.convertTo_F32(output, null, true);
        panel.addImage(shape, "Shape");
      }

      ShowImages.showWindow(panel, getClass().getSimpleName(), true);
    }

    return null;
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
    new FindCardShadingFeatures().find(args[0], true);
  }
}
