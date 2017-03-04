package com.tom_e_white.set_game;

public class Card {
    public enum Number {
        THREE, ONE, TWO; // so that the ordinals make sense (mod 3)

        static Number parseDescription(String description) {
            int num = Integer.parseInt(description.split(" ")[0]);
            return values()[num % 3];
        }
    }
    public enum Shading {
        EMPTY, STRIPED, SOLID;

        static Shading parseDescription(String description) {
            return valueOf(description.split(" ")[1].toUpperCase());
        }
    }
    public enum Color {
        GREEN, PURPLE, RED;

        static Color parseDescription(String description) {
            return valueOf(description.split(" ")[2].toUpperCase());
        }
    }
    public enum Shape {
        DIAMOND, OVAL, SQUIGGLE;

        static Shape parseDescription(String description) {
            return valueOf(description.split(" ")[3].toUpperCase().replaceFirst("S$", ""));
        }
    }
}
