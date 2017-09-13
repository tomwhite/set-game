package com.tom_e_white.set_game.predict;

import com.tom_e_white.set_game.preprocess.CardDetector;
import com.tom_e_white.set_game.preprocess.CardImage;
import com.tom_e_white.set_game.train.FindCardColourFeatures;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class PredictCardColorConvNet {
  public static void main(String[] args) throws IOException {
    File testFile = new File(args[0]);
    CardDetector cardDetector = new CardDetector();
    List<CardImage> images = cardDetector.detect(testFile.getAbsolutePath(), false);
    List<String> testDescriptions = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());
    FindCardColourFeatures colourFinder = new FindCardColourFeatures(2); // to parse description

    ConvNet convNet = new ConvNet("tf/set_color.pb", "tf/set_color.txt");

    int correct = 0;
    int total = 0;
    for (int i = 0; i < testDescriptions.size(); i++) {
      float[] labelProbabilities = convNet.predict(images.get(i).getImage());
      //System.out.println(Arrays.toString(labelProbabilities));
      int predictedLabel = ConvNet.maxIndex(labelProbabilities);
      int actualLabel = colourFinder.getLabelFromDescription(testDescriptions.get(i));
      if (predictedLabel == actualLabel) {
        correct++;
      } else {
        System.out.println("Incorrect, predicted " + predictedLabel + " but was " + actualLabel + " for card " + (i + 1));
      }
      total++;
    }
    System.out.println("Correct: " + correct);
    System.out.println("Total: " + total);
    double accuracy = ((double) correct)/total * 100;
    System.out.println("Accuracy: " + ((int) accuracy) + " percent");
    System.out.println("------------------------------------------");
  }
}
