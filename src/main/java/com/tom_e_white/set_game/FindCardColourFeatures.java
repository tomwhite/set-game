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

public class FindCardColourFeatures extends FeatureFinder {

    @Override
    public int getLabel(String filename) {
        return CardLabel.getColourNumber(new File(filename));
    }

    @Override
    public int getLabelNumberFromLabel(String label) {
        return CardLabel.getColourNumber(label);
    }

    @Override
    public double[] find(BufferedImage image, boolean debug) throws IOException {
        return ImageUtils.coupledHueSat(image);
    }

    @Override
    public String getFileSuffix() {
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
        System.out.println(featureFinder.getSummaryLine(args[0], features));
    }
}
