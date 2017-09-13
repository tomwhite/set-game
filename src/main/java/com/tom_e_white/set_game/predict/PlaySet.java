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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Use {@link CardPredictor} to play Set using a webcam, or another image source, such as a file.
 */
public class PlaySet implements Runnable{

    private final Supplier<BufferedImage> imageSupplier;
    private final CardPredictor cardPredictor;
    private final ImagePanel panel;
    private final boolean streaming;

    public PlaySet(Supplier<BufferedImage> imageSupplier, boolean streaming, boolean debug) throws IOException {
        this.imageSupplier = imageSupplier;
        this.cardPredictor = new CardPredictorConvNet();
        this.panel = new ImagePanel(annotateImage(imageSupplier.get(), debug));
        this.streaming = streaming;
    }

    @Override
    public void run() {
        if (!streaming) {
            addMouseListener(panel);
        }
        ShowImages.showWindow(panel, PlaySet.class.getSimpleName(), true);

        if (streaming) {
            while (true) {
                newImage(imageSupplier.get());
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
        System.out.println("Time to detect cards: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        List<CardPrediction> cardPredictions = images.stream().map(cardImage -> {
            try {
                return cardPredictor.predict(cardImage);
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        System.out.println("Time to predict cards: " + (System.currentTimeMillis() - start));

        Graphics2D g2 = image.createGraphics();
        g2.setStroke(new BasicStroke(10));
        g2.setColor(Color.BLUE);

        if (cardPredictions.isEmpty()) {
            System.out.println("No cards found in image");
        } else if (cardPredictions.size() < 3) {
            System.out.println("Too few cards found in image");
        } else {
            System.out.println(cardPredictions);
            SetPredictor setPredictor = new SetPredictor();
            List<SetPrediction> setPredictions = setPredictor.predict(cardPredictions);
            setPredictions.forEach(System.out::println);
            if (setPredictions.isEmpty()) {
                System.out.println("No sets found");
            } else {
                Triple set = setPredictions.get(0).getSet();
                Map<Card, CardImage> cardToImageMap = new LinkedHashMap<>();
                for (int i = 0; i < cardPredictions.size(); i++) {
                    cardToImageMap.put(cardPredictions.get(i).getCard(), images.get(i));
                }
                VisualizeShapes.draw(cardToImageMap.get(set.first()).getExternalQuadrilateral(), g2);
                VisualizeShapes.draw(cardToImageMap.get(set.second()).getExternalQuadrilateral(), g2);
                VisualizeShapes.draw(cardToImageMap.get(set.third()).getExternalQuadrilateral(), g2);
            }
        }

        return image;
    }

    public static void main(String[] args) throws Exception {
        // "http://192.168.1.67:8080/shot.jpg"
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
