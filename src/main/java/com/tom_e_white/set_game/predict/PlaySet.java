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
import com.tom_e_white.set_game.model.Triple;
import com.tom_e_white.set_game.preprocess.CardDetector;
import com.tom_e_white.set_game.preprocess.CardImage;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.*;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

/**
 * Use {@link CardPredictor} to play Set using a webcam, or another image source, such as a file.
 */
public class PlaySet implements Runnable{

    private final Supplier<BufferedImage> imageSupplier;
    private final CardPredictor cardPredictor;
    private final ImagePanel panel;
    private final boolean streaming;
    private BufferedImage image;
    private Triple previousSet;

    public PlaySet(Supplier<BufferedImage> imageSupplier, boolean streaming, boolean debug) throws IOException {
        this.imageSupplier = imageSupplier;
        this.cardPredictor = new CardPredictorConvNet();
        this.image = imageSupplier.get();
        this.panel = new ImagePanel(1280, 720);
        this.panel.setBufferedImageSafe(annotateImage(image, debug));
        this.streaming = streaming;
    }

    @Override
    public void run() {
        if (!streaming) {
            addMouseListener(panel);
        }
        JFrame window = ShowImages.showWindow(panel, PlaySet.class.getSimpleName(),true);
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == 'x' && previousSet != null) {
                    System.out.println("'Not a Set!'");
                    ImageSuppliers.WebcamSaverImageSupplier.save(image);
                }
            }
        });

        if (streaming) {
            while (true) {
                image = imageSupplier.get();
                newImage(image);
            }
        }
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
        panel.setBufferedImageSafe(annotateImage(image, false));
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

    public BufferedImage annotateImage(BufferedImage image, boolean debug) {
        CardDetector cardDetector = new CardDetector(66);
        long start = System.currentTimeMillis();
        List<CardImage> images;
        try {
            images = cardDetector.detect(image, null, debug, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //System.out.println("Time to detect cards: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        List<CardPrediction> cardPredictions = images.stream().map(cardImage -> {
            try {
                return cardPredictor.predict(cardImage);
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        //System.out.println("Time to predict cards: " + (System.currentTimeMillis() - start));

        if (cardPredictions.isEmpty()) {
            System.out.println("No cards found in image");
            previousSet = null;
        } else if (cardPredictions.size() < 3) {
            System.out.println("Too few cards found in image");
            previousSet = null;
        } else {
            System.out.println(cardPredictions);
            SetPredictor setPredictor = new SetPredictor();
            List<SetPrediction> setPredictions = setPredictor.predict(cardPredictions);
            if (setPredictions.isEmpty()) {
                System.out.println("No sets found");
            } else {
                Map<Card, CardImage> cardToImageMap = new LinkedHashMap<>();
                for (int i = 0; i < cardPredictions.size(); i++) {
                    cardToImageMap.put(cardPredictions.get(i).getCard(), images.get(i));
                }
                image = highlightSets(setPredictions, cardToImageMap, image, 1);
            }
        }
        return image;
    }

    private BufferedImage highlightSets(List<SetPrediction> setPredictions, Map<Card, CardImage> cardToImageMap, BufferedImage image, int maxToShow) {
        BufferedImage annotatedImage = deepCopy(image);
        Graphics2D g2 = annotatedImage.createGraphics();
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        Color[] colors = new Color[] {
                Color.BLUE, Color.RED, Color.GREEN
        };
        List<Triple> setsToShow = new ArrayList<>();
        SetPrediction prediction = setPredictions.get(0);
        if (previousSet == null || !previousSet.equals(prediction.getSet())) {
            System.out.println("Best set: " + prediction);
        }
        float topProbability = prediction.getProbability();
        previousSet = prediction.getSet();
        for (SetPrediction setPrediction : setPredictions) {
            if (setPrediction.getProbability() < topProbability) {
                break;
            }
            setsToShow.add(setPrediction.getSet());
        }
        for (int i = 0; i < setsToShow.size(); i++) {
            if (i == maxToShow) {
                break;
            }
            Triple set = setsToShow.get(i);
            g2.setStroke(new BasicStroke(8 - (i * 2)));
            g2.setColor(colors[i % colors.length]);
            VisualizeShapes.draw(cardToImageMap.get(set.first()).getExternalQuadrilateral(), g2);
            VisualizeShapes.draw(cardToImageMap.get(set.second()).getExternalQuadrilateral(), g2);
            VisualizeShapes.draw(cardToImageMap.get(set.third()).getExternalQuadrilateral(), g2);
        }
        return annotatedImage;
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null)
                .getSubimage(0, 0, bi.getWidth(), bi.getHeight());
    }

    public static void main(String[] args) throws Exception {
        // "http://192.168.1.67:8080/shot.jpg"
        // "http://192.168.1.92:8080/shot.jpg"
        boolean debug = false;
        Supplier<BufferedImage> imageSupplier = new ImageSuppliers.ReverseChronologicalSavedImageSupplier();
        boolean streaming = false;
        if (args.length > 0) {
            if (args[0].equals("webcam")) {
                String url = args.length == 1 ? null : args[1];
                imageSupplier = new ImageSuppliers.WebcamImageSupplier(findWebcam(url));
                streaming = true;
            } else if (args[0].equals("webcamsaver")) {
                String url = args.length == 1 ? null : args[1];
                imageSupplier = new ImageSuppliers.WebcamSaverImageSupplier(findWebcam(url));
            } else if (args[0].equals("reverse")) {
                imageSupplier = new ImageSuppliers.ReverseChronologicalSavedImageSupplier();
            } else if (args[0].equals("random")) {
                imageSupplier = new ImageSuppliers.RandomSavedImageSupplier();
            } else if (args[0].equals("file")) {
                imageSupplier = new ImageSuppliers.NamedSavedImageSupplier(new File(args[1]).getAbsolutePath());
            }
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new PlaySet(imageSupplier, streaming, debug));
    }
}
