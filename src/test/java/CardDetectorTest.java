import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CardDetectorTest {
    @Test
    public void test() throws IOException {
        CardDetector cardDetector = new CardDetector();
        File[] files = new File("data").listFiles((dir, name) -> name.matches("2016.*\\.jpg"));
        for (File file : files) {
            System.out.println(file);
            List<BufferedImage> images = cardDetector.scan(file.getAbsolutePath());
            assertTrue(images.size() > 0);
        }
    }
}
