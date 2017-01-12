package com.tom_e_white.set_game;

import boofcv.alg.misc.PixelMath;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;

import java.awt.image.BufferedImage;

public class CardDiff {
    public static void main(String[] args) {
        //BufferedImage image1 = UtilImageIO.loadImage("data/train-out/green-1-20161231_114459_1.jpg");
        BufferedImage image1 = UtilImageIO.loadImage("data/train-out/green-1-20161231_114459_4.jpg");

        //BufferedImage image2 = UtilImageIO.loadImage("data/train-out/green-1-20161231_114506_1.jpg");
        //BufferedImage image2 = UtilImageIO.loadImage("data/train-out/green-1-20161231_114459_2.jpg");
        BufferedImage image2 = UtilImageIO.loadImage("data/train-out/green-1-20161231_114506_4.jpg");

        GrayU8 gray1 = ConvertBufferedImage.convertFromSingle(image1, null, GrayU8.class);
        GrayU8 gray2 = ConvertBufferedImage.convertFromSingle(image2, null, GrayU8.class);
        GrayU8 diff = gray1.createSameShape();

        PixelMath.diffAbs(gray1, gray2, diff);

        ListDisplayPanel panel = new ListDisplayPanel();
        panel.addImage(image1, "Image 1");
        panel.addImage(image2, "Image 2");
        panel.addImage(gray1, "Gray 1");
        panel.addImage(gray2, "Gray 2");
        panel.addImage(diff, "Diff");

        ShowImages.showWindow(panel, "Binary Operations", true);
    }
}
