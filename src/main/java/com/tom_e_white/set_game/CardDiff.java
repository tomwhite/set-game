package com.tom_e_white.set_game;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.alg.misc.PixelMath;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;

import java.awt.image.BufferedImage;

public class CardDiff {
    public static void main(String[] args) {
        //BufferedImage image1 = UtilImageIO.loadImage("data/train-out/green-1-20161231_114459_1.jpg");
        BufferedImage image1 = UtilImageIO.loadImage("data/train-out/green-1-20161231_114459_4.jpg");
        //BufferedImage image1 = UtilImageIO.loadImage("data/train-out/green-1-20170101_124953_7.jpg");

        //BufferedImage image2 = UtilImageIO.loadImage("data/train-out/green-1-20161231_114506_1.jpg");
        BufferedImage image2 = UtilImageIO.loadImage("data/train-out/green-1-20161231_114459_2.jpg");
        //BufferedImage image2 = UtilImageIO.loadImage("data/train-out/green-1-20161231_114506_4.jpg");

        GrayU8 gray1 = preprocess(image1);
        GrayU8 gray2 = preprocess(image2);
        GrayU8 diff = gray1.createSameShape();

        PixelMath.diffAbs(gray1, gray2, diff);
        System.out.println("Diff abs: " + ImageStatistics.mean(diff));

        BufferedImage visualBinary1 = VisualizeBinaryData.renderBinary(gray1, false, null);
        BufferedImage visualBinary2 = VisualizeBinaryData.renderBinary(gray2, false, null);
        BufferedImage visualBinaryDiff = VisualizeBinaryData.renderBinary(diff, false, null);

        ListDisplayPanel panel = new ListDisplayPanel();
        panel.addImage(image1, "Image 1");
        panel.addImage(image2, "Image 2");
        panel.addImage(visualBinary1, "Gray 1");
        panel.addImage(visualBinary2, "Gray 2");
        panel.addImage(visualBinaryDiff, "Diff");

        ShowImages.showWindow(panel, "Binary Operations", true);
    }

    private static GrayU8 preprocess(BufferedImage image) {
        GrayU8 gray = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
        GrayU8 gaussian = BlurImageOps.gaussian(gray, null, 0, 4, null);
        GrayU8 median = BlurImageOps.median(gray, null, 4);
        double threshold = GThresholdImageOps.computeOtsu(gray, 0, 255);
        System.out.println("threshold: " + threshold);
        GrayU8 binary = gray.createSameShape();
        GrayU8 hi = gray.createSameShape();
        ThresholdImageOps.threshold(gray, binary, (int) threshold, true);// "down=false" to find exterior contours

        PixelMath.multiply(binary, 255, hi);
        System.out.println(gray.get(5, 5));
        System.out.println(binary.get(5, 5));
        System.out.println(hi.get(5, 5));

        //GrayU8 filtered = BinaryImageOps.erode8(gray, 1, null);
        GrayU8 filtered = BinaryImageOps.dilate8(gray, 1, null);

        return gray;
    }
}
