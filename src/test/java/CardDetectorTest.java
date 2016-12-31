import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class CardDetectorTest {
    @Test
    public void test() throws IOException {
        CardDetector cardDetector = new CardDetector();
        File[] files = new File("data").listFiles((dir, name) -> name.matches("2016.*\\.jpg"));
        for (File file : files) {
            System.out.println(file);
            cardDetector.scan(file.getAbsolutePath());
        }
    }
}
