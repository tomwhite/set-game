package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.preprocess.CardImage;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CardPredictorConvNetPerAttribute extends CardPredictor {
  @Override
  public CardPrediction predict(CardImage cardImage) throws IOException {
    BufferedImage image = cardImage.getImage();

    float[] numberLabelProbabilities = PredictCardColorConvNet.predict(image, "tf/set_number.pb");
    int predictedNumberLabel = PredictCardColorConvNet.maxIndex(numberLabelProbabilities);

    float[] colorLabelProbabilities = PredictCardColorConvNet.predict(image, "tf/set_color.pb");
    int predictedColorLabel = PredictCardColorConvNet.maxIndex(colorLabelProbabilities);

    float[] shadingLabelProbabilities = PredictCardColorConvNet.predict(image, "tf/set_shading.pb");
    int predictedShadingLabel = PredictCardColorConvNet.maxIndex(shadingLabelProbabilities);

    float[] shapeLabelProbabilities = PredictCardColorConvNet.predict(image, "tf/set_shape.pb");
    int predictedShapeLabel = PredictCardColorConvNet.maxIndex(shapeLabelProbabilities);

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
}
