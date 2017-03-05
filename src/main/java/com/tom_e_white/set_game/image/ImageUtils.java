package com.tom_e_white.set_game.image;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.feature.color.GHistogramFeatureOps;
import boofcv.alg.feature.color.Histogram_F64;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.misc.PixelMath;
import boofcv.core.image.ConvertImage;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import georegression.metric.UtilAngle;

import java.awt.image.BufferedImage;

public class ImageUtils {

    // From ExampleColorHistogramLookup in Boofcv

    /**
     * HSV stores color information in Hue and Saturation while intensity is in Value.  This computes a 2D histogram
     * from hue and saturation only, which makes it lighting independent.
     */
    public static double[] coupledHueSat(BufferedImage image) {
        Planar<GrayF32> rgb = new Planar<>(GrayF32.class, image.getWidth(), image.getHeight(), 3);
        Planar<GrayF32> hsv = new Planar<>(GrayF32.class, image.getWidth(), image.getHeight(), 3);

        ConvertBufferedImage.convertFrom(image, rgb, true);
        ColorHsv.rgbToHsv_F32(rgb, hsv);

        Planar<GrayF32> hs = hsv.partialSpectrum(0, 1);

        // The number of bins is an important parameter.  Try adjusting it
        Histogram_F64 histogram = new Histogram_F64(5, 5);
        histogram.setRange(0, 0, 2.0 * Math.PI); // range of hue is from 0 to 2PI
        histogram.setRange(1, 0, 1.0);         // range of saturation is from 0 to 1

        // Compute the histogram
        GHistogramFeatureOps.histogram(hs, histogram);

        UtilFeature.normalizeL2(histogram); // normalize so that image size doesn't matter

        return histogram.value;
    }

    public static BufferedImage filterBackgroundOut(BufferedImage image) {

        Planar<GrayF32> input = ConvertBufferedImage.convertFromMulti(image, null, true, GrayF32.class);
        Planar<GrayF32> hsv = new Planar<>(GrayF32.class, input.getWidth(), input.getHeight(), 3);

        // Convert into HSV
        ColorHsv.rgbToHsv_F32(input, hsv);

        // Euclidean distance squared threshold for deciding which pixels are members of the selected set
        float maxDist2 = 0.4f * 0.4f;

        // Extract hue and saturation bands which are independent of intensity
        GrayF32 H = hsv.getBand(0);
        GrayF32 S = hsv.getBand(1);

        float hue = H.get(1, 1);
        float saturation = S.get(1, 1);

        // Adjust the relative importance of Hue and Saturation.
        // Hue has a range of 0 to 2*PI and Saturation from 0 to 1.
        float adjustUnits = (float) (Math.PI / 2.0);

        // step through each pixel and mark how close it is to the selected color
        BufferedImage output = new BufferedImage(input.width, input.height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < hsv.height; y++) {
            for (int x = 0; x < hsv.width; x++) {
                // Hue is an angle in radians, so simple subtraction doesn't work
                float dh = UtilAngle.dist(H.unsafe_get(x, y), hue);
                float ds = (S.unsafe_get(x, y) - saturation) * adjustUnits;

                // this distance measure is a bit naive, but good enough for to demonstrate the concept
                float dist2 = dh * dh + ds * ds;
                if (dist2 > maxDist2 * 4) {
                    output.setRGB(x, y, image.getRGB(x, y));
                }
            }
        }
        return output;
    }
    public static BufferedImage maskBackground(BufferedImage image) {
        GrayU8 gray = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
        int threshold = gray.get(1, 1); // get background pixel - would be better to average some
        GrayU8 binary = ThresholdImageOps.threshold(gray, null, threshold, true);
        GrayF32 mask = ConvertImage.convert(binary, (GrayF32) null);
        return mask(image, mask);
    }

    private static BufferedImage mask(BufferedImage image, GrayF32 mask) {
        Planar<GrayF32> input = ConvertBufferedImage.convertFromMulti(image, null, true, GrayF32.class);
        Planar<GrayF32> output = new Planar<>(GrayF32.class, input.getWidth(), input.getHeight(), 3);
        for (int i = 0; i < input.getNumBands(); i++) {
            PixelMath.multiply(input.getBand(i), mask, output.getBand(i));
        }
        return ConvertBufferedImage.convertTo_F32(output, null, true);
    }
}