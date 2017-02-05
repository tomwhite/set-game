package com.tom_e_white.set_game;

import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use {@link FindCardShapeFeatures} to predict the shape of each card in the test set.
 */
public class PredictCardShapeOnTestData {

    public static double predict(File testFile) throws IOException {
        FindCardShapeFeatures featureFinder = new FindCardShapeFeatures();
        NearestNeighbor<Integer> nn = FactoryNearestNeighbor.exhaustive();
        nn.init(2);
        List<Integer> labels = new ArrayList<>();
        List<double[]> vectors = new ArrayList<>();
        List<String> lines = Files.lines(Paths.get("data/train-out-" + featureFinder.getFileName())).collect(Collectors.toList());
        for (String line : lines) {
            LabelledVector labelledVector = featureFinder.getLabelledVector(line);
            labels.add(labelledVector.getLabel());
            vectors.add(labelledVector.getVector());
        }
        nn.setPoints(vectors, labels);

        CardDetector cardDetector = new CardDetector();
        List<BufferedImage> images = cardDetector.scan(testFile.getAbsolutePath(), true);
        List<String> testLabels = Files.lines(Paths.get(testFile.getAbsolutePath().replace(".jpg", ".txt"))).collect(Collectors.toList());

        int correct = 0;
        int total = 0;
        for (int i = 0; i < testLabels.size(); i++) {
            double[] features = featureFinder.find(images.get(i), false);
            FastQueue<NnData<Integer>> results = new FastQueue(NnData.class,true);
            nn.findNearest(features, -1, 5, results);
            int predictedLabel = getMostFrequent(results);
            int actualLabel = CardLabel.getShapeNumber(testLabels.get(i));
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
        return accuracy;
    }

    private static Integer getMostFrequent(FastQueue<NnData<Integer>> results) {
        Map<Integer, Long> collect = results.toList().stream()
                .map(d -> d.data)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        Optional<Long> max = collect.values().stream().max(Long::compareTo);
        for (Map.Entry<Integer, Long> e : collect.entrySet()) {
            if (e.getValue().equals(max.get())) {
                return e.getKey();
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(predict(new File(args[0])));
    }
}
