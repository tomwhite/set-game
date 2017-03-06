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
        // order signifies how training data cards were laid out
        // solid, striped, empty
        // oval, diamond, squiggle
        switch (index) {
            case 1: return toDescription(number, Shading.SOLID, colour, Shape.OVAL);
            case 2: return toDescription(number, Shading.SOLID, colour, Shape.DIAMOND);
            case 3: return toDescription(number, Shading.SOLID, colour, Shape.SQUIGGLE);
            case 4: return toDescription(number, Shading.STRIPED, colour, Shape.OVAL);
            case 5: return toDescription(number, Shading.STRIPED, colour, Shape.DIAMOND);
            case 6: return toDescription(number, Shading.STRIPED, colour, Shape.SQUIGGLE);
            case 7: return toDescription(number, Shading.EMPTY, colour, Shape.OVAL);
            case 8: return toDescription(number, Shading.EMPTY, colour, Shape.DIAMOND);
            case 9: return toDescription(number, Shading.EMPTY, colour, Shape.SQUIGGLE);
            default: throw new IllegalArgumentException("Unrecognized index " + index);
        }
    }

    private static String toDescription(int number, Shading shading, String colour, Shape shape) {
        String s = number == 1 ? "" : "s";
        return number + " " + shading.name().toLowerCase() + " " + colour + " " + shape.name().toLowerCase() + s;
    }

    private final Number number;
    private final Shading shading;
    private final Color color;
    private final Shape shape;

    public Card(String description) {
        this.number = Number.parseDescription(description);
        this.shading = Shading.parseDescription(description);
        this.color = Color.parseDescription(description);
        this.shape = Shape.parseDescription(description);
    }

    public Card(int numberLabel, int shadingLabel, int colorLabel, int shapeLabel) {
        this.number = Number.values()[numberLabel];
        this.shading = Shading.values()[shadingLabel];
        this.color = Color.values()[colorLabel];
        this.shape = Shape.values()[shapeLabel];
    }

    public Number number() { return number; }
    public Shading shading() { return shading; }
    public Color color() { return color; }
    public Shape shape() { return shape; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (number != card.number) return false;
        if (shading != card.shading) return false;
        if (color != card.color) return false;
        return shape == card.shape;
    }

    @Override
    public int hashCode() {
        int result = number.hashCode();
        result = 31 * result + shading.hashCode();
        result = 31 * result + color.hashCode();
        result = 31 * result + shape.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Card{" +
                "number=" + number +
                ", shading=" + shading +
                ", color=" + color +
                ", shape=" + shape +
                '}';
    }
}
