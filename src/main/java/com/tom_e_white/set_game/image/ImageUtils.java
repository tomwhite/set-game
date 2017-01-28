package com.tom_e_white.set_game.image;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.feature.color.GHistogramFeatureOps;
import boofcv.alg.feature.color.Histogram_F64;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;

import java.awt.image.BufferedImage;

public class ImageUtils {

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
