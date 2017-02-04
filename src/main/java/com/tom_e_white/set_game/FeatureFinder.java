package com.tom_e_white.set_game;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface FeatureFinder {
    int getLabel(String filename);
    double[] find(BufferedImage image, boolean debug) throws IOException;
    String getFileName();
    String getSummaryLine(String filename, double[] features);
}
