package fr.mazure.vision;

import java.io.IOException;
import java.nio.file.Path;

public class WebVisionAnalyzer {
    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar web-vision-analyzer.jar <url>");
            System.exit(1);
            return;
        }
        final String url = args[0];
        try {
            analyzeWebPage(url);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void analyzeWebPage(final String url) throws IOException {
        final int id = Screenshooter.generateScreenshot(url);
        final Path screenshotPath = Screenshooter.getScreenshotPath(id);
        final String json = GoogleVision.analyzeImageWithVision(screenshotPath);
        System.out.println(json);
    }
}