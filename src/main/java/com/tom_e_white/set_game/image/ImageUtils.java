package com.tom_e_white.set_game.image;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.feature.color.GHistogramFeatureOps;
import boofcv.alg.feature.color.Histogram_F64;
import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

import java.awt.image.BufferedImage;

public class ImageUtils {
    public static GrayU8 medianBlur(GrayU8 input, int radius) {
        // size of the blur kernel. square region with a width of radius*2 + 1
        return BlurImageOps.median(input, null, radius);
    }

    public static GrayU8 binarize(GrayU8 input, int minValue, int maxValue) {
        return binarize(input, minValue, maxValue, false); // "down=false" to find exterior contours
    }

    public static GrayU8 binarize(GrayU8 input, int minValue, int maxValue, boolean down) {
        // Select a global threshold using Otsu's method.
        double threshold = GThresholdImageOps.computeOtsu(input, minValue, maxValue);

        // Apply the threshold to create a binary image
        return ThresholdImageOps.threshold(input, null, (int) threshold, down); // "down=false" to find exterior contours
    }

    public static GrayU8 edges(GrayU8 input) {
        GrayU8 edgeImage = input.createSameShape();
        CannyEdge<GrayU8, GrayS16> canny = FactoryEdgeDetectors.canny(2, true, true, GrayU8.class, GrayS16.class);
        canny.process(input, 0.1f, 0.2f, edgeImage);
        return edgeImage;
    }

    // From ExampleColorHistogramLookup in Boofcv
    /**
     * HSV stores color information in Hue and Saturation while intensity is in Value.  This computes a 2D histogram
     * from hue and saturation only, which makes it lighting independent.
     */
    public static double[] coupledHueSat(BufferedImage image) {
        Planar<GrayF32> rgb = new Planar<>(GrayF32.class, image.getWidth(), image.getHeight(),3);
        Planar<GrayF32> hsv = new Planar<>(GrayF32.class, image.getWidth(), image.getHeight(),3);

        ConvertBufferedImage.convertFrom(image, rgb, true);
        ColorHsv.rgbToHsv_F32(rgb, hsv);

        Planar<GrayF32> hs = hsv.partialSpectrum(0,1);

        // The number of bins is an important parameter.  Try adjusting it
        Histogram_F64 histogram = new Histogram_F64(12,12);
        histogram.setRange(0, 0, 2.0*Math.PI); // range of hue is from 0 to 2PI
        histogram.setRange(1, 0, 1.0);         // range of saturation is from 0 to 1

        // Compute the histogram
        GHistogramFeatureOps.histogram(hs,histogram);

        UtilFeature.normalizeL2(histogram); // normalize so that image size doesn't matter

        return histogram.value;
    }
}
