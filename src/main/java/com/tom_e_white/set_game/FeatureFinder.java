package com.tom_e_white.set_game;

import java.io.IOException;

public interface FeatureFinder<F extends Features> {
    F find(String filename, boolean debug) throws IOException;
}
