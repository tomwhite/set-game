package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.preprocess.CardImage;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class CardPredictorConvNet extends CardPredictor implements Closeable {

  private final ConvNet convNet;

  public CardPredictorConvNet() throws IOException {
    this.convNet = new ConvNet("tf/set_all.pb", "tf/set_all.txt");
  }

  @Override
  public CardPrediction predict(CardImage cardImage) throws IOException {
    BufferedImage image = cardImage.getImage();
    float[] labelProbabilities = convNet.predict(image);
    //System.out.println(Arrays.toString(labelProbabilities));
    int predictedLabel = ConvNet.maxIndex(labelProbabilities);
    List<String> labels = convNet.getLabels();
    return new CardPrediction(new Card(labels.get(predictedLabel).replace("-", " ")), labelProbabilities[predictedLabel]);
  }

  @Override
  public void close() throws IOException {
    convNet.close();
  }
}
