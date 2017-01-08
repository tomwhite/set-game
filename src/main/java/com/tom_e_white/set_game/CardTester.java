package com.tom_e_white.set_game;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.misc.ImageStatistics;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CardTester {

    public static class ScoredCard implements Comparable<ScoredCard> {
        private String label;
        private String filename;
        private double score;

        public ScoredCard(String label, String filename, double score) {
            this.label = label;
            this.filename = filename;
            this.score = score;
        }

        @Override
        public String toString() {
            return "ScoredCard{" +
                    "label='" + label + '\'' +
                    ", filename='" + filename + '\'' +
                    ", score=" + score +
                    '}';
        }

        @Override
        public int compareTo(ScoredCard o) {
            return Double.compare(score, o.score);
        }
    }

    public static void main(String[] args) throws IOException {
        CardDetector cardDetector = new CardDetector();
        List<BufferedImage> images = cardDetector.scan(args[0], true);
        for (BufferedImage image : images) {
            Arrays.stream(new File("data/train-out").listFiles((dir, name) -> name.matches(".*\\.jpg"))).map(
                    f -> new ScoredCard(toLabel(f.getName()), f.toString(), diff(image, UtilImageIO.loadImage(f.toString())))
            ).sorted().forEach(System.out::println);
//            System.out.println(best.get());
            break;
        }
//
//        String green_1_1 = "/Users/tom/projects-workspace/set-game/data/train-out/green-1-20161231_114459_1.jpg";
//        String green_1_2 = "/Users/tom/projects-workspace/set-game/data/train-out/green-1-20161231_114459_2.jpg";
//        String purple_3_1 = "/Users/tom/projects-workspace/set-game/data/train-out/purple-3-20161231_114125_1.jpg";
//
//        BufferedImage image1 = UtilImageIO.loadImage(green_1_1);
//        BufferedImage image2 = UtilImageIO.loadImage(green_1_2);
//        BufferedImage image3 = UtilImageIO.loadImage(purple_3_1);
//
//        Arrays.stream(new File("data/train-out").listFiles((dir, name) -> name.matches(".*\\.jpg"))).map(
//                f -> new ScoredCard(toLabel(f.getName()), f.toString(), diff(image3, UtilImageIO.loadImage(f.toString())))
//        ).sorted().forEach(System.out::println);
    }

    private static double diff(BufferedImage image1, BufferedImage image2) {
        Planar<GrayF32> input1 = ConvertBufferedImage.convertFromMulti(image1, null, true, GrayF32.class);
        Planar<GrayF32> input2 = ConvertBufferedImage.convertFromMulti(image2, null, true, GrayF32.class);

        // try using HS (and not V)
//        Planar<GrayF32> hsv1 = new Planar<>(GrayF32.class,input1.getWidth(),input1.getHeight(),3);
//        Planar<GrayF32> hsv2 = new Planar<>(GrayF32.class,input2.getWidth(),input2.getHeight(),3);
//
//        ColorHsv.rgbToHsv_F32(input1, hsv1);
//        ColorHsv.rgbToHsv_F32(input2, hsv2);
//
//        Planar<GrayF32> hs1 = hsv1.partialSpectrum(0,1);
//        Planar<GrayF32> hs2 = hsv2.partialSpectrum(0,1);
//
//        double d1 = ImageStatistics.meanDiffAbs(hs1.getBand(0), hs2.getBand(0));
//        double d2 = ImageStatistics.meanDiffAbs(hs1.getBand(1), hs2.getBand(1));
//        return Math.sqrt(d1 * d1 + d2 * d2);

//        input1 = hsv1;
//        input2 = hsv2;
//
        double d1 = ImageStatistics.meanDiffAbs(input1.getBand(0), input2.getBand(0));
        double d2 = ImageStatistics.meanDiffAbs(input1.getBand(1), input2.getBand(1));
        double d3 = ImageStatistics.meanDiffAbs(input1.getBand(2), input2.getBand(2));
        return Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
    }

    public static String toLabel(String filename) {
        String reg = "([^-]+)-([^-]+).*(\\d)\\.jpg";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(filename);
        if (matcher.matches()) {
            String colour = matcher.group(1);
            String number = matcher.group(2);
            String index = matcher.group(3);
            return number + colour.charAt(0) + toLabel(Integer.parseInt(index));
        }
        throw new IllegalArgumentException("Unrecognized file: " + filename);
    }

    private static String toLabel(int index) {
        // filled, hatched, open
        // oval, diamond, squiggle
        switch (index) {
            case 1: return "fo";
            case 2: return "fd";
            case 3: return "fs";
            case 4: return "ho";
            case 5: return "hd";
            case 6: return "hs";
            case 7: return "oo";
            case 8: return "od";
            case 9: return "os";
            default: throw new IllegalArgumentException("Unrecognized index " + index);
        }
    }
}
