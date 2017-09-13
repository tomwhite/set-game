package com.tom_e_white.set_game.predict;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

public class ConvNet implements Closeable {

  private static final String INPUT_OPERATION_NAME = "conv2d_1_input";
  private static final String KERAS_LEARNING_PHASE_OPERATION_NAME = "keras_learning_phase";
  private static final String OUTPUT_OPERATION_NAME = "dense_2/Softmax";

  private final Session imageNormalizationSession;
  private String normalizationOutputOperationName;

  private final Session session;

  private final List<String> labels;

  public ConvNet(String modelFile, String labelsFile) throws IOException {
    this.imageNormalizationSession = new Session(constructGraphToNormalizeImage());
    this.session = new Session(constructGraphFromModel(modelFile));
    this.labels = Files.readAllLines(Paths.get(labelsFile), Charset.forName("UTF-8"));
  }

  private Graph constructGraphToNormalizeImage() {
    Graph graph = new Graph();
    GraphBuilder b = new GraphBuilder(graph);
    // - The model was trained with images scaled to 150x150 pixels.
    // - The colors, represented as R, G, B in 1-byte each were converted to
    //   float using value/Scale.
    final int H = 150;
    final int W = 150;
    final float scale = 255f;

    final Output input = b.placeholder("input", DataType.STRING);
    final Output output =
        b.div(
            b.resizeBilinear(
                b.expandDims(
                    b.cast(b.decodeJpeg(input, 3), DataType.FLOAT),
                    b.constant("make_batch", 0)),
                b.constant("size", new int[] {H, W})),
            b.constant("scale", scale));
    normalizationOutputOperationName = output.op().name();
    return graph;
  }

  private Graph constructGraphFromModel(String modelFile) throws IOException {
    byte[] graphDef = Files.readAllBytes(Paths.get(modelFile));
    Graph graph = new Graph();
    graph.importGraphDef(graphDef);
    return graph;
  }

  public List<String> getLabels() {
    return labels;
  }

  public float[] predict(BufferedImage image) throws IOException {
    try (Tensor imageTensor = executeGraphToNormalizeImage(asJpegByteArray(image))) {
      return executeCnnGraph(imageTensor);
    }
  }

  public static int maxIndex(float[] probabilities) {
    int best = 0;
    for (int i = 1; i < probabilities.length; ++i) {
      if (probabilities[i] > probabilities[best]) {
        best = i;
      }
    }
    return best;
  }

  private byte[] asJpegByteArray(BufferedImage image) throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      ImageIO.write(image, "jpg", out);
      return out.toByteArray();
    }
  }

  private Tensor executeGraphToNormalizeImage(byte[] imageBytes) {
    try (Tensor t = Tensor.create(imageBytes)) {
      return imageNormalizationSession.runner()
          .feed("input", t)
          .fetch(normalizationOutputOperationName).run().get(0);
    }
  }

  private float[] executeCnnGraph(Tensor image) {
    try (Tensor result = session.runner()
        .feed(INPUT_OPERATION_NAME, image)
        .feed(KERAS_LEARNING_PHASE_OPERATION_NAME, Tensor.create(false))
        .fetch(OUTPUT_OPERATION_NAME).run().get(0)) {
      final long[] rshape = result.shape();
      if (result.numDimensions() != 2 || rshape[0] != 1) {
        throw new RuntimeException(
            String.format(
                "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                Arrays.toString(rshape)));
      }
      int nlabels = (int) rshape[1];
      return result.copyTo(new float[1][nlabels])[0];
    }
  }

  @Override
  public void close() throws IOException {
    imageNormalizationSession.close();
    session.close();
  }

  static class GraphBuilder {
    GraphBuilder(Graph g) {
      this.g = g;
    }

    Output div(Output x, Output y) {
      return binaryOp("Div", x, y);
    }

    Output resizeBilinear(Output images, Output size) {
      return binaryOp("ResizeBilinear", images, size);
    }

    Output expandDims(Output input, Output dim) {
      return binaryOp("ExpandDims", input, dim);
    }

    Output cast(Output value, DataType dtype) {
      return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0);
    }

    Output decodeJpeg(Output contents, long channels) {
      return g.opBuilder("DecodeJpeg", "DecodeJpeg")
          .addInput(contents)
          .setAttr("channels", channels)
          .build()
          .output(0);
    }

    Output constant(String name, Object value) {
      try (Tensor t = Tensor.create(value)) {
        return g.opBuilder("Const", name)
            .setAttr("dtype", t.dataType())
            .setAttr("value", t)
            .build()
            .output(0);
      }
    }

    Output placeholder(String name, DataType dataType) {
      return g.opBuilder("Placeholder", name)
          .setAttr("dtype", dataType)
          .build()
          .output(0);
    }

    private Output binaryOp(String type, Output in1, Output in2) {
      return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
    }

    private Graph g;
  }
}
