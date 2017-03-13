package com.tom_e_white.set_game.train;

import boofcv.alg.misc.ImageStatistics;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import com.tom_e_white.set_game.image.GeometryUtils;
import com.tom_e_white.set_game.image.ImageProcessingPipeline;
import com.tom_e_white.set_game.image.Shape;
import com.tom_e_white.set_game.model.Card;
import georegression.struct.shapes.Quadrilateral_F64;
import georegression.struct.shapes.RectangleLength2D_F32;
import smile.classification.Classifier;
import smile.classification.KNN;
import smile.data.AttributeDataset;
import smile.data.NominalAttribute;
import smile.data.parser.DelimitedTextParser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * Find features for shading on a card.
 */
public class FindCardShadingFeatures extends FeatureFinder {

  private Classifier<double[]> classifier;

  @Override
  public int getLabelFromFilename(String filename) {
    return Card.Shading.parseFilename(new File(filename)).ordinal();
  }

  @Override
  public int getLabelFromDescription(String description) {
    return Card.Shading.parseDescription(description).ordinal();
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

    Optional<RectangleLength2D_F32> box = nonOverlapping.stream()
            .map(Shape::getBoundingBox)
            .findFirst();

    double[] features = null;
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

      GrayU8 finalImage = ImageProcessingPipeline.fromBufferedImage(shape, panel)
              .gray()
              .sharpen()
              .binarize(0, 255, true)
              .extract(100 - 25, 50 - 12, 50, 25)
              .getImage();
      double meanPixelValue = ImageStatistics.mean(finalImage);
      features = new double[] { meanPixelValue };
    }

    if (debug) {
      ShowImages.showWindow(panel, getClass().getSimpleName(), true);
    }

    return features;
  }

  @Override
  public String getFileSuffix() {
    return "shading.csv";
  }

  @Override
  public Classifier<double[]> getClassifier() throws IOException, ParseException {
    if (classifier != null) {
      return classifier;
    }
    DelimitedTextParser parser = new DelimitedTextParser();
    parser.setDelimiter(",");
    parser.setResponseIndex(new NominalAttribute("shading", new String[] { "0", "1", "2" }), 0);
    AttributeDataset dataset = parser.parse("data/train-out-" + getFileSuffix());
    double[][] vectors = dataset.toArray(new double[dataset.size()][]);
    int[] label = dataset.toArray(new int[dataset.size()]);
    classifier = KNN.learn(vectors, label, 5);
    return classifier;
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
    BufferedImage image = UtilImageIO.loadImage(args[0]);
    FindCardShadingFeatures featureFinder = new FindCardShadingFeatures();
    double[] features = featureFinder.find(image, true);
    System.out.println(featureFinder.getSummaryLine(args[0], features));
  }
}
