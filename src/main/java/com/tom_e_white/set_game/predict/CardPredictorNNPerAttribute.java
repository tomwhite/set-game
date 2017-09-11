package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.preprocess.CardImage;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CardPredictorNNPerAttribute extends CardPredictor {
  @Override
  public CardPrediction predict(CardImage cardImage) throws IOException {
    BufferedImage image = cardImage.getImage();

    float[] numberLabelProbabilities = PredictCardColorNN.predict(image, "set_number.pb");
    int predictedNumberLabel = PredictCardColorNN.maxIndex(numberLabelProbabilities);

    float[] colorLabelProbabilities = PredictCardColorNN.predict(image, "set_color.pb");
    int predictedColorLabel = PredictCardColorNN.maxIndex(colorLabelProbabilities);

    float[] shadingLabelProbabilities = PredictCardColorNN.predict(image, "set_shading.pb");
    int predictedShadingLabel = PredictCardColorNN.maxIndex(shadingLabelProbabilities);

    float[] shapeLabelProbabilities = PredictCardColorNN.predict(image, "set_shape.pb");
    int predictedShapeLabel = PredictCardColorNN.maxIndex(shapeLabelProbabilities);

    float probability = numberLabelProbabilities[predictedNumberLabel] *
        colorLabelProbabilities[predictedColorLabel] *
        shadingLabelProbabilities[predictedShadingLabel] *
        shapeLabelProbabilities[predictedShapeLabel];

    // TODO: make ordering consistent
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
