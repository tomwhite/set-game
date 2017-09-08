package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.model.Card;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class CardPredictorNN extends CardPredictor {
  @Override
  public Card predict(BufferedImage image) throws IOException, ParseException {
    float[] labelProbabilities = PredictCardColorNN.predict(image, "set_all.pb");
    //System.out.println(Arrays.toString(labelProbabilities));
    int predictedLabel = PredictCardColorNN.maxIndex(labelProbabilities);
    List<String> labels = PredictCardColorNN.readLabels("set_all.txt");
    return new Card(labels.get(predictedLabel).replace("-", " "));
  }
}
