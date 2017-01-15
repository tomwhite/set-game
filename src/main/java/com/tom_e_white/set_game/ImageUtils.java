package com.tom_e_white.set_game;

import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.struct.image.GrayS16;
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
        return ThresholdImageOps.threshold(input, null, (int) threshold, true); // "down=false" to find exterior contours
    }

    public static GrayU8 findEdges(GrayU8 input) {
        GrayU8 edgeImage = input.createSameShape();
        CannyEdge<GrayU8, GrayS16> canny = FactoryEdgeDetectors.canny(2, true, true, GrayU8.class, GrayS16.class);
        canny.process(input, 0.1f, 0.2f, edgeImage);
        return edgeImage;
    }
}
