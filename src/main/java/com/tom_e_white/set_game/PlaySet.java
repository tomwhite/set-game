package com.tom_e_white.set_game;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import com.tom_e_white.set_game.model.Card;
import com.tom_e_white.set_game.model.Cards;
import com.tom_e_white.set_game.model.Triple;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use {@link CardPredictor} to play Set.
 */
public class PlaySet {

    public static void play(File testFile) throws IOException, ParseException {
        CardDetector cardDetector = new CardDetector();
        CardPredictor cardPredictor = new CardPredictor();
        List<CardImage> images = cardDetector.detect(testFile.getAbsolutePath(), false);

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

        Set<Triple> sets = Cards.sets(cards);

        sets.forEach(System.out::println);

        ListDisplayPanel panel = new ListDisplayPanel();
        BufferedImage mainImage = UtilImageIO.loadImage(testFile.getAbsolutePath());
        Graphics2D g2 = mainImage.createGraphics();
        g2.setStroke(new BasicStroke(10));
        g2.setColor(Color.BLUE);
        Triple set = new ArrayList<>(sets).get(1); // TODO: check if exists

        VisualizeShapes.draw(cardToImageMap.get(set.first()).getExternalQuadrilateral(), g2);
        VisualizeShapes.draw(cardToImageMap.get(set.second()).getExternalQuadrilateral(), g2);
        VisualizeShapes.draw(cardToImageMap.get(set.third()).getExternalQuadrilateral(), g2);

        panel.addImage(mainImage, "Game");
        ShowImages.showWindow(panel, PlaySet.class.getSimpleName(), true);
    }

    public static void main(String[] args) throws Exception {
        play(new File(args[0]));
    }
}
