import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CardDetectorTest {

    @Test
    public void testNoCards() throws IOException {
        CardDetector cardDetector = new CardDetector();
        List<BufferedImage> images = cardDetector.scan("data/egg.jpg");
        assertTrue(images.isEmpty());
    }

    @Test
    public void testSingleCard() throws IOException {
        CardDetector cardDetector = new CardDetector();
        List<BufferedImage> images = cardDetector.scan("data/one-card-20161230_192626.jpg");
        assertEquals(1, images.size());
    }

    @Test
    public void testErrantBorder() throws IOException {
        // this image has a border with some artifacts that may be detected as quadrilaterals
        CardDetector cardDetector = new CardDetector();
        List<BufferedImage> images = cardDetector.scan("data/train/purple-1-20170101_124559.jpg");
        assertEquals(9, images.size());
    }

    @Test
    public void testTrainingImagesContainNineCards() {
        CardDetector cardDetector = new CardDetector();
        Arrays.stream(new File("data/train").listFiles((dir, name) -> name.matches(".*\\.jpg"))).forEach(
                file -> {
                    try {
                        System.out.println(file);
                        List<BufferedImage> images = cardDetector.scan(file.getAbsolutePath());
                        assertEquals(9, images.size());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
