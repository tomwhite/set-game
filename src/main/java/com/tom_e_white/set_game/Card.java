package com.tom_e_white.set_game;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Card {
    public enum Number {
        THREE, ONE, TWO; // so that the ordinals make sense (mod 3)

        public static Number parseDescription(String description) {
            int num = Integer.parseInt(description.split(" ")[0]);
            return values()[num % 3];
        }
        public static Number parseFilename(File file) {
            return parseDescription(toDescription(file));
        }
    }
    public enum Shading {
        EMPTY, STRIPED, SOLID;

        public static Shading parseDescription(String description) {
            return valueOf(description.split(" ")[1].toUpperCase());
        }
        public static Shading parseFilename(File file) {
            return parseDescription(toDescription(file));
        }
    }
    public enum Color {
        GREEN, PURPLE, RED;

        public static Color parseDescription(String description) {
            return valueOf(description.split(" ")[2].toUpperCase());
        }
        public static Color parseFilename(File file) {
            return parseDescription(toDescription(file));
        }
    }
    public enum Shape {
        DIAMOND, OVAL, SQUIGGLE;

        public static Shape parseDescription(String description) {
            return valueOf(description.split(" ")[3].toUpperCase().replaceFirst("S$", ""));
        }
        public static Shape parseFilename(File file) {
            return parseDescription(toDescription(file));
        }
    }

    private static String toDescription(File file) {
        String reg = "([^-]+)-([^-]+).*(\\d)\\.jpg";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.matches()) {
            String colour = matcher.group(1);
            String number = matcher.group(2);
            String index = matcher.group(3);
            return toDescription(Integer.parseInt(number), colour, Integer.parseInt(index));
        }
        throw new IllegalArgumentException("Unrecognized file: " + file);
    }

    private static String toDescription(int number, String colour, int index) {
        // order on how training data cards were laid out
        // solid, striped, empty
        // oval, diamond, squiggle
        String s = number == 1 ? "" : "s";
        switch (index) {
            case 1: return number + " solid " + colour + " oval" + s;
            case 2: return number + " solid " + colour + " diamond" + s;
            case 3: return number + " solid " + colour + " squiggle" + s;
            case 4: return number + " striped " + colour + " oval" + s;
            case 5: return number + " striped " + colour + " diamond" + s;
            case 6: return number + " striped " + colour + " squiggle" + s;
            case 7: return number + " empty " + colour + " oval" + s;
            case 8: return number + " empty " + colour + " diamond" + s;
            case 9: return number + " empty " + colour + " squiggle" + s;
            default: throw new IllegalArgumentException("Unrecognized index " + index);
        }
    }
}
