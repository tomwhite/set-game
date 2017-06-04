package com.tom_e_white.set_game.predict;

import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;
import com.tom_e_white.set_game.image.ImageSuppliers;
import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.model.Cards;
import com.tom_e_white.set_game.model.Triple;
import com.tom_e_white.set_game.preprocess.CardDetector;
import com.tom_e_white.set_game.preprocess.CardImage;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Use {@link CardPredictor} to play Set using a webcam, or another image source, such as a file.
 */
public class PlaySet {

    private final Supplier<BufferedImage> imageSupplier;
    private final ImagePanel panel;

    public PlaySet(Supplier<BufferedImage> imageSupplier, boolean debug) {
        this.imageSupplier = imageSupplier;
        this.panel = new ImagePanel(annotateImage(imageSupplier.get(), debug));
        addMouseListener(panel);
        ShowImages.showWindow(panel, PlaySet.class.getSimpleName(), true);
    }

    private void addMouseListener(ImagePanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    System.out.println("Next image");
                    newImage(imageSupplier.get());
                }
            }
        });
    }

    private void newImage(BufferedImage image) {
        panel.setBufferedImage(annotateImage(image, false));
    }

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

    public static BufferedImage annotateImage(BufferedImage image, boolean debug) {
        CardDetector cardDetector = new CardDetector(4, 66); // TODO: blur radius should be a function of image size
        CardPredictor cardPredictor = new CardPredictor(2);
        List<CardImage> images;
        try {
            images = cardDetector.detect(image, null, debug, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

        Graphics2D g2 = image.createGraphics();
        g2.setStroke(new BasicStroke(10));
        g2.setColor(Color.BLUE);

        if (cards.isEmpty()) {
            System.out.println("No cards found in image");
        } else {
            System.out.println(cards);
            Set<Triple> sets = Cards.sets(cards);
            sets.forEach(System.out::println);
            if (sets.isEmpty()) {
                System.out.println("No sets found");
            } else {
                Triple set = new ArrayList<>(sets).get(0);
                VisualizeShapes.draw(cardToImageMap.get(set.first()).getExternalQuadrilateral(), g2);
                VisualizeShapes.draw(cardToImageMap.get(set.second()).getExternalQuadrilateral(), g2);
                VisualizeShapes.draw(cardToImageMap.get(set.third()).getExternalQuadrilateral(), g2);
            }
        }

        return image;
    }

    public static void main(String[] args) throws Exception {
        // "http://192.168.1.92:8080/shot.jpg"
        boolean debug = false;
        Supplier<BufferedImage> imageSupplier = new ImageSuppliers.ReverseChronologicalSavedImageSupplier();
        if (args.length > 0) {
            if (args[0].equals("webcam")) {
                String url = args.length == 1 ? null : args[1];
                imageSupplier = new ImageSuppliers.WebcamImageSupplier(findWebcam(url));
            } else if (args[0].equals("reverse")) {
                imageSupplier = new ImageSuppliers.ReverseChronologicalSavedImageSupplier();
            } else if (args[0].equals("random")) {
                imageSupplier = new ImageSuppliers.RandomSavedImageSupplier();
            } else if (args[0].equals("file")) {
                imageSupplier = new ImageSuppliers.NamedSavedImageSupplier(new File(args[1]).getAbsolutePath());
            }
        }
        new PlaySet(imageSupplier, debug);
    }
}
