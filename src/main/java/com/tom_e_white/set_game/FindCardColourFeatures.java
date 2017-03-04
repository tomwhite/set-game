package com.tom_e_white.set_game;

import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.image.ImageUtils;
import smile.classification.Classifier;
import smile.classification.KNN;
import smile.data.AttributeDataset;
import smile.data.NominalAttribute;
import smile.data.parser.DelimitedTextParser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

public class FindCardColourFeatures implements FeatureFinder {

    @Override
    public int getLabel(String filename) {
        return CardLabel.getColourNumber(new File(filename));
    }

    @Override
    public int getLabelNumberFromLabel(String label) {
        return CardLabel.getColourNumber(label);
    }

    @Override
    public String getSummaryLine(int label, double[] features) {
        StringBuilder sb = new StringBuilder().append(label);
        for (double f : features) {
            sb.append(",").append(f);
        }
        return sb.toString();
    }

    @Override
    public String getSummaryLine(String filename, double[] features) {
        return getSummaryLine(getLabel(filename), features);
    }

    @Override
    public LabelledVector getLabelledVector(String summaryLine) {
        String[] split = summaryLine.split(",");
        double[] vector = new double[split.length - 1];
        for (int i = 0; i < split.length - 1; i++) {
            vector[i] = Double.parseDouble(split[i + 1]);
        }
        return new LabelledVector(Integer.parseInt(split[0]), vector);
    }

    @Override
    public double[] find(BufferedImage image, boolean debug) throws IOException {
        return ImageUtils.coupledHueSat(image);
    }

    @Override
    public String getFileName() {
        return "colour.csv";
    }

    @Override
    public Classifier<double[]> getClassifier() throws IOException, ParseException {
        DelimitedTextParser parser = new DelimitedTextParser();
        parser.setDelimiter(",");
        parser.setResponseIndex(new NominalAttribute("colour", new String[] { "1", "2", "3" }), 0);
        AttributeDataset dataset = parser.parse("data/train-out-colour.csv");
        double[][] vectors = dataset.toArray(new double[dataset.size()][]);
        int[] label = dataset.toArray(new int[dataset.size()]);
        return KNN.learn(vectors, label, 25);
    }

    public static void main(String[] args) throws IOException {
        BufferedImage image = UtilImageIO.loadImage(args[0]);
        FindCardColourFeatures featureFinder = new FindCardColourFeatures();
        double[] features = featureFinder.find(image, true);
        System.out.println(featureFinder.getLabel(args[0]));
        System.out.println(Arrays.toString(features));
    }
}
