package com.tom_e_white.set_game.train;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.image.ImageProcessingPipeline;
import com.tom_e_white.set_game.model.Card;
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

    private final int version;
    private Classifier<double[]> classifier;

    public FindCardColourFeatures() {
        this(1);
    }

    public FindCardColourFeatures(int version) {
        this.version = version;
    }

    @Override
    public int getLabelFromFilename(String filename) {
        if (version == 1) {
            Card.Color.parseFilename(new File(filename)).ordinal();
        }
        return Card.Color.parseDescription(new File(filename).getParentFile().getName().replace("-", " ")).ordinal();
    }

    @Override
    public int getLabelFromDescription(String description) {
        return Card.Color.parseDescription(description).ordinal();
    }

    @Override
    public double[] find(BufferedImage image, boolean debug) throws IOException {
        ListDisplayPanel panel = debug ? new ListDisplayPanel() : null;
        double[] features = ImageProcessingPipeline.fromBufferedImage(image, panel)
                .filterBackgroundOut()
                .coupledHueSat();
        if (debug) {
            ShowImages.showWindow(panel, getClass().getSimpleName(), true);
        }
        return features;
    }

    @Override
    public String getFileSuffix() {
        return version == 1 ? "colour.csv" : "colour-" + version + ".csv";
    }

    @Override
    public Classifier<double[]> getClassifier() throws IOException, ParseException {
        if (classifier != null) {
            return classifier;
        }
        DelimitedTextParser parser = new DelimitedTextParser();
        parser.setDelimiter(",");
        parser.setResponseIndex(new NominalAttribute("colour", new String[] { "0", "1", "2" }), 0);
        AttributeDataset dataset = parser.parse("data/train-out-" + getFileSuffix());
        double[][] vectors = dataset.toArray(new double[dataset.size()][]);
        int[] label = dataset.toArray(new int[dataset.size()]);
        classifier = KNN.learn(vectors, label, 100);
        return classifier;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage image = UtilImageIO.loadImage(args[0]);
        FindCardColourFeatures featureFinder = new FindCardColourFeatures(2);
        double[] features = featureFinder.find(image, true);
        System.out.println(featureFinder.getSummaryLine(args[0], features));
    }
}
