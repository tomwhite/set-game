package com.tom_e_white.set_game;

import java.io.File;
import java.io.IOException;

public class CardFeatureCounterBatch {
    public static void main(String[] args) throws IOException {
        CardFeatureCounter cardFeatureCounter = new CardFeatureCounter();
        int correct = 0;
        int total = 0;
        for (File file : new File("data/train-out").listFiles((dir, name) -> name.matches(".*\\.jpg"))) {
            System.out.println(file);
            int predictedNumber = cardFeatureCounter.scan(file.getAbsolutePath());
            int actualNumber = getShapeNumber(file);
            if (predictedNumber == actualNumber) {
                correct++;
            } else {
                System.out.println("Incorrect, predicted " + predictedNumber);
            }
            total++;
        }
        System.out.println("Correct: " + correct);
        System.out.println("Total: " + total);
        System.out.println("Accuracy: " + ((int) (((double) correct)/total * 100)) + " percent");    }

    private static int getShapeNumber(File file) {
        return Integer.parseInt(CardPredictor.toLabel(file).split(" ")[0]);
    }

}
