package com.tom_e_white.set_game;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Methods for working with card labels.
 */
public class CardLabel {
    static String toLabel(File file) {
        return toLabel(file.getName());
    }
    static String toLabel(String filename) {
        String reg = "([^-]+)-([^-]+).*(\\d)\\.jpg";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(filename);
        if (matcher.matches()) {
            String colour = matcher.group(1);
            String number = matcher.group(2);
            String index = matcher.group(3);
            return toLabel(Integer.parseInt(number), colour, Integer.parseInt(index));
        }
        throw new IllegalArgumentException("Unrecognized file: " + filename);
    }

    private static String toLabel(int number, String colour, int index) {
        // filled, hatched, open
        // oval, diamond, squiggle
        String s = number == 1 ? "" : "s";
        switch (index) {
            case 1: return number + " filled " + colour + " oval" + s;
            case 2: return number + " filled " + colour + " diamond" + s;
            case 3: return number + " filled " + colour + " squiggle" + s;
            case 4: return number + " hatched " + colour + " oval" + s;
            case 5: return number + " hatched " + colour + " diamond" + s;
            case 6: return number + " hatched " + colour + " squiggle" + s;
            case 7: return number + " open " + colour + " oval" + s;
            case 8: return number + " open " + colour + " diamond" + s;
            case 9: return number + " open " + colour + " squiggle" + s;
            default: throw new IllegalArgumentException("Unrecognized index " + index);
        }
    }

    static int getNumber(File file) {
        return Integer.parseInt(toLabel(file).split(" ")[0]);
    }

    static int getShadingNumber(String label) {
        String shadingString = label.split(" ")[1];
        switch (shadingString) {
            case "filled": return 1;
            case "hatched": return 2;
            case "open": return 3;
            default: throw new IllegalArgumentException("Unrecognized shading: " + shadingString);
        }
    }

    static int getShadingNumber(File file) {
        return getShadingNumber(toLabel(file.getName()));
    }

    static int getColourNumber(File file) {
        String colourString = toLabel(file).split(" ")[2];
        switch (colourString) {
            case "red": return 1;
            case "purple": return 2;
            case "green": return 3;
            default: throw new IllegalArgumentException("Unrecognized colour: " + colourString);
        }
    }

    static int getShapeNumber(File file) {
        String shapeString = toLabel(file).split(" ")[3];
        switch (shapeString) {
            case "oval": case "ovals": return 1;
            case "diamond": case "diamonds": return 2;
            case "squiggle": case "squiggles": return 3;
            default: throw new IllegalArgumentException("Unrecognized shape: " + shapeString);
        }
    }
}
