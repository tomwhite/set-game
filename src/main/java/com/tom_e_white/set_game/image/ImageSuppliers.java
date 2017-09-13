package com.tom_e_white.set_game.image;

import boofcv.io.image.UtilImageIO;
import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

public class ImageSuppliers {

    private static final File WEBCAM_DIR = new File("data/webcam");


    public static class ReverseChronologicalSavedImageSupplier implements Supplier<BufferedImage> {

        private final File[] files = listSorted();
        private int index = files.length - 1;

        static File[] listSorted() {
            File[] files = WEBCAM_DIR.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
            Arrays.sort(files);
            return files;
        }

        @Override
        public BufferedImage get() {
            if (files.length == 0) {
                return null;
            }
            File file = files[index];
            index--;
            if (index < 0) {
                index = files.length - 1;
            }
            System.out.println("Using " + file);
            return UtilImageIO.loadImage(file.getAbsolutePath());
        }
    }

    public static class RandomSavedImageSupplier implements Supplier<BufferedImage> {
        private final Random r = new Random();

        @Override
        public BufferedImage get() {
            File[] files = WEBCAM_DIR.listFiles();
            if (files.length == 0) {
                return null;
            }
            File file = files[r.nextInt(files.length)];
            System.out.println("Using " + file);
            return UtilImageIO.loadImage(file.getAbsolutePath());
        }
    }

    public static class NamedSavedImageSupplier implements Supplier<BufferedImage> {

        private String file;

        public NamedSavedImageSupplier(String file) {
            this.file = file;
        }

        @Override
        public BufferedImage get() {
            return UtilImageIO.loadImage(file);
        }
    }

    public static class WebcamImageSupplier implements Supplier<BufferedImage> {

        private Webcam webcam;

        public WebcamImageSupplier(Webcam webcam) {
            this.webcam = webcam;
        }

        @Override
        public BufferedImage get() {
            return webcam.getImage();
        }
    }

    public static class WebcamSaverImageSupplier implements Supplier<BufferedImage> {

        private Webcam webcam;

        public WebcamSaverImageSupplier(Webcam webcam) {
            this.webcam = webcam;
        }

        @Override
        public BufferedImage get() {
            BufferedImage image = webcam.getImage();
            DateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
            File file = new File(WEBCAM_DIR, format.format(new Date()) + ".png");
            try {
                ImageIO.write(image, "PNG", file);
                System.out.println("Saved image to " + file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return image;
        }
    }
}
