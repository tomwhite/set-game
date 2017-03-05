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
    public int getLabelFromFilename(String filename) {
        return Card.Color.parseFilename(new File(filename)).ordinal();
    }

    @Override
    public int getLabelFromDescription(String description) {
        return Card.Color.parseDescription(description).ordinal();
    }

    @Override
    public double[] find(BufferedImage image, boolean debug) throws IOException {
        BufferedImage filtered = ImageUtils.filterBackgroundOut(image);
        return ImageUtils.coupledHueSat(filtered);
    }

    @Override
    public String getFileSuffix() {
        return "colour.csv";
    }

    @Override
    public Classifier<double[]> getClassifier() throws IOException, ParseException {
        DelimitedTextParser parser = new DelimitedTextParser();
        parser.setDelimiter(",");
        parser.setResponseIndex(new NominalAttribute("colour", new String[] { "0", "1", "2" }), 0);
        AttributeDataset dataset = parser.parse("data/train-out-" + getFileSuffix());
        double[][] vectors = dataset.toArray(new double[dataset.size()][]);
        int[] label = dataset.toArray(new int[dataset.size()]);
        return KNN.learn(vectors, label, 100);
    }

    public static void main(String[] args) throws IOException {
        BufferedImage image = UtilImageIO.loadImage(args[0]);
        FindCardColourFeatures featureFinder = new FindCardColourFeatures();
        double[] features = featureFinder.find(image, true);
        System.out.println(featureFinder.getSummaryLine(args[0], features));
    }
}
