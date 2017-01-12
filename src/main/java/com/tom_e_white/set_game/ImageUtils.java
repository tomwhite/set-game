package com.tom_e_white.set_game;

import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.struct.image.GrayU8;

public class ImageUtils {
    public static GrayU8 medianBlur(GrayU8 input, int radius) {
        // size of the blur kernel. square region with a width of radius*2 + 1
        return BlurImageOps.median(input, null, radius);
    }

    public static GrayU8 binarize(GrayU8 input, int minValue, int maxValue) {
        // Select a global threshold using Otsu's method.
        double threshold = GThresholdImageOps.computeOtsu(input, 0, 255);

        // Apply the threshold to create a binary image
        return ThresholdImageOps.threshold(input, null, (int) threshold, false); // "down=false" to find exterior contours
    }
}
