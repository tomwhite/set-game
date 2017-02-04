package com.tom_e_white.set_game;

import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use {@link FindCardShadingFeatures} to predict the shading on each card in the test set.
 */
public class PredictCardShadingOnTestData {

    public static boolean predict(File testFile) throws IOException {
        NearestNeighbor<Integer> nn = FactoryNearestNeighbor.exhaustive();
        nn.init(1);
        List<Integer> labels = new ArrayList<>();
        List<double[]> points = new ArrayList<>();
        List<String> lines = Files.lines(Paths.get("data/train-out-shading.csv")).collect(Collectors.toList());
        for (String line : lines) {
            String[] split = line.split(",");
            labels.add(Integer.parseInt(split[0]));
            double[] values = new double[split.length - 1];
            for (int i = 0; i < split.length - 1; i++) {
                values[i] = Double.parseDouble(split[i + 1]);
            }
            points.add(values);
        }
        FindCardShadingFeatures featureFinder = new FindCardShadingFeatures();
        FindCardShadingFeatures.CardShadingFeatures features = featureFinder.find(testFile.getAbsolutePath(), false);
        FastQueue<NnData<Integer>> results = new FastQueue(NnData.class,true);
        nn.findNearest(new double[] { features.getMeanPixelValue() }, -1, 5, results);
        int predictedNumber = getMostFrequent(results);
        int actualNumber = CardLabel.getShadingNumber(testFile);
        return predictedNumber == actualNumber;
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
