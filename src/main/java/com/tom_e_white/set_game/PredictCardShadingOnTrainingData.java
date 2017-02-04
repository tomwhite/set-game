package com.tom_e_white.set_game;

import boofcv.io.image.UtilImageIO;
import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use {@link FindCardShadingFeatures} to predict the shading on each card in 10% of the held-back training set.
 */
public class PredictCardShadingOnTrainingData {

    public static double computeAccuracyOnTrainingData() throws IOException {
        NearestNeighbor<Integer> nn = FactoryNearestNeighbor.exhaustive();
        nn.init(1);
        List<double[]> points = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();
        FindCardShadingFeatures featureFinder = new FindCardShadingFeatures();
        List<File> files = Arrays.asList(new File("data/train-out").listFiles((dir, name) -> name.matches(".*\\.jpg")));
        Collections.shuffle(files);
        List<File> testFiles = files.subList(0, files.size()/10);
        List<File> trainingFiles = files.subList(files.size()/10, files.size());
        for (File file : trainingFiles) {
            System.out.println(file);
            double[] features = featureFinder.find(UtilImageIO.loadImage(file.getAbsolutePath()), false);
            if (features == null) {
                continue;
            }
            points.add(features);
            labels.add(CardLabel.getShadingNumber(file));
        }
        nn.setPoints(points, labels);

        int correct = 0;
        int total = 0;
        for (File testFile : testFiles) {
            double[] features = featureFinder.find(UtilImageIO.loadImage(testFile.getAbsolutePath()), false);
            FastQueue<NnData<Integer>> results = new FastQueue(NnData.class,true);
            nn.findNearest(features, -1, 5, results);
            int predictedNumber = getMostFrequent(results);
            int actualNumber = CardLabel.getShadingNumber(testFile);
            if (predictedNumber == actualNumber) {
                correct++;
            } else {
                System.out.println("Incorrect, predicted " + predictedNumber);
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
        computeAccuracyOnTrainingData();
    }
}
