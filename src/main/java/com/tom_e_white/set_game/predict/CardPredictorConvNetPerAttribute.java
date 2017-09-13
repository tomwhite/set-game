package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.preprocess.CardImage;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;

public class CardPredictorConvNetPerAttribute extends CardPredictor implements Closeable {

  private final ConvNet numberConvNet;
  private final ConvNet colorConvNet;
  private final ConvNet shadingConvNet;
  private final ConvNet shapeConvNet;

  public CardPredictorConvNetPerAttribute() throws IOException {
    this.numberConvNet = new ConvNet("tf/set_number.pb", "tf/set_number.txt");
    this.colorConvNet = new ConvNet("tf/set_color.pb", "tf/set_color.txt");
    this.shadingConvNet = new ConvNet("tf/set_shading.pb", "tf/set_shading.txt");
    this.shapeConvNet = new ConvNet("tf/set_shape.pb", "tf/set_shape.txt");
  }

  @Override
  public CardPrediction predict(CardImage cardImage) throws IOException {
    BufferedImage image = cardImage.getImage();

    float[] numberLabelProbabilities = numberConvNet.predict(image);
    int predictedNumberLabel = ConvNet.maxIndex(numberLabelProbabilities);

    float[] colorLabelProbabilities = colorConvNet.predict(image);
    int predictedColorLabel = ConvNet.maxIndex(colorLabelProbabilities);

    float[] shadingLabelProbabilities = shadingConvNet.predict(image);
    int predictedShadingLabel = ConvNet.maxIndex(shadingLabelProbabilities);

    float[] shapeLabelProbabilities = shapeConvNet.predict(image);
    int predictedShapeLabel = ConvNet.maxIndex(shapeLabelProbabilities);

    float probability = numberLabelProbabilities[predictedNumberLabel] *
        colorLabelProbabilities[predictedColorLabel] *
        shadingLabelProbabilities[predictedShadingLabel] *
        shapeLabelProbabilities[predictedShapeLabel];

    // Change ordering from lexicographic to "The Joy of Set" order
    predictedNumberLabel = (predictedNumberLabel + 1) % 3;
    if (predictedShadingLabel == 1) {
      predictedShadingLabel = 2;
    } else if (predictedShadingLabel == 2) {
      predictedShadingLabel = 1;
    }
    Card card = new Card(predictedNumberLabel, predictedColorLabel, predictedShadingLabel, predictedShapeLabel);

    return new CardPrediction(card, probability);
  }

  @Override
  public void close() throws IOException {
    numberConvNet.close();
    colorConvNet.close();
    shadingConvNet.close();
    shapeConvNet.close();
  }
}
