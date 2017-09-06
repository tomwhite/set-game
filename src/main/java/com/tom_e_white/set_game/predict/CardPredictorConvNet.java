package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.preprocess.CardImage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class CardPredictorConvNet extends CardPredictor {
  @Override
  public CardPrediction predict(CardImage cardImage) throws IOException {
    BufferedImage image = cardImage.getImage();
    float[] labelProbabilities = PredictCardColorConvNet.predict(image, "tf/set_all.pb");
    //System.out.println(Arrays.toString(labelProbabilities));
    int predictedLabel = PredictCardColorConvNet.maxIndex(labelProbabilities);
    List<String> labels = PredictCardColorConvNet.readLabels("tf/set_all.txt");
    return new CardPrediction(new Card(labels.get(predictedLabel).replace("-", " ")), labelProbabilities[predictedLabel]);
  }
}
