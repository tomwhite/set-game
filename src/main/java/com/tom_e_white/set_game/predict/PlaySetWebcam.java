package com.tom_e_white.set_game.predict;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;
import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.model.Cards;
import com.tom_e_white.set_game.model.Triple;
import com.tom_e_white.set_game.preprocess.CardDetector;
import com.tom_e_white.set_game.preprocess.CardImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use {@link CardPredictor} to play Set using a webcam.
 */
public class PlaySetWebcam {

    private static Webcam findWebcam(String url) throws MalformedURLException {
        if (url != null) {
            Webcam.setDriver(new IpCamDriver());
            IpCamDeviceRegistry.register(new IpCamDevice("Set", url, IpCamMode.PULL));
        }
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new IllegalStateException("Cannot find webcam");
        }
        webcam.open();
        return webcam;
    }

    public static void play(File file, boolean debug) throws IOException, ParseException {
        play(UtilImageIO.loadImage(file.getAbsolutePath()), debug);
    }

    public static void play(BufferedImage image, boolean debug) throws IOException, ParseException {
        CardDetector cardDetector = new CardDetector(4, 66); // TODO: blur radius should be a function of image size
        CardPredictor cardPredictor = new CardPredictor(2);
        List<CardImage> images = cardDetector.detect(image, null,debug, true);

        List<Card> cards = images.stream().map(cardImage -> {
            try {
                return cardPredictor.predict(cardImage.getImage());
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        Map<Card, CardImage> cardToImageMap = new LinkedHashMap<>();
        for (int i = 0; i < cards.size(); i++) {
            cardToImageMap.put(cards.get(i), images.get(i));
        }

        System.out.println(cards);

        Set<Triple> sets = Cards.sets(cards);

        sets.forEach(System.out::println);

        ListDisplayPanel panel = new ListDisplayPanel();
        Graphics2D g2 = image.createGraphics();
        g2.setStroke(new BasicStroke(10));
        g2.setColor(Color.BLUE);
        Triple set = new ArrayList<>(sets).get(1); // TODO: check if exists

        VisualizeShapes.draw(cardToImageMap.get(set.first()).getExternalQuadrilateral(), g2);
        VisualizeShapes.draw(cardToImageMap.get(set.second()).getExternalQuadrilateral(), g2);
        VisualizeShapes.draw(cardToImageMap.get(set.third()).getExternalQuadrilateral(), g2);

        panel.addImage(image, "Game");
        ShowImages.showWindow(panel, PlaySetWebcam.class.getSimpleName(), true);
    }

    public static void main(String[] args) throws Exception {
        // "http://192.168.1.92:8080/shot.jpg"
        boolean debug = true;
        File webcamDir = new File("data/webcam");
        if (debug) {
            Optional<File> mostRecent = Arrays
                            .stream(webcamDir.listFiles())
                            .max(Comparator.comparingLong(File::lastModified));
            if (mostRecent.isPresent()) {
                play(mostRecent.get(), debug);
            } else {
                System.out.println("No files to debug in " + webcamDir);
            }
        } else {
            DateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
            File file = new File(webcamDir, format.format(new Date()) + ".png");
            Webcam webcam = findWebcam(args.length == 0 ? null : args[0]);
            ImageIO.write(webcam.getImage(), "PNG", file);
            play(webcam.getImage(), debug);
        }
    }
}
