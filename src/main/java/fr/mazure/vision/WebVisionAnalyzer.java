package fr.mazure.vision;

import com.microsoft.playwright.*;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.*;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.google.cloud.vision.v1.Feature.Type;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class WebVisionAnalyzer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java -jar web-vision-analyzer.jar <url>");
            System.exit(1);
            return;
        }
        String url = args[0];
        analyzeWebPage(url);
    }

    public static void analyzeWebPage(String url) {
        try (final Playwright playwright = Playwright.create()) {
            // Launch browser in headless mode
            final Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));

            // Create a new browser context
            final BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1920, 1080));

            // Create a new page
            final Page page = context.newPage();

            // Navigate to the URL
            page.navigate(url);
            
            // Wait for the network to be idle
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Take screenshot
            final Path screenshotPath = Paths.get("screenshots", "webpage_screenshot.png");
            page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath)
                                                        .setFullPage(true));

            // Analyze the screenshot with Google Cloud Vision
            analyzeImageWithVision(screenshotPath);

            // Clean up
            browser.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyzeImageWithVision(Path imagePath) {
        try {
            try (final ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
                // Read the image file
                final byte[] imageBytes = Files.readAllBytes(imagePath);
                final ByteString imgBytes = ByteString.copyFrom(imageBytes);

                // Create image object
                final Image img = Image.newBuilder().setContent(imgBytes).build();

                // Create feature list for different types of detection
                final List<Feature> features = new ArrayList<>();

                // Add text detection
                features.add(Feature.newBuilder().setType(Type.TEXT_DETECTION).build());

                // Add object detection (can help identify buttons and UI elements)
                features.add(Feature.newBuilder().setType(Type.OBJECT_LOCALIZATION).build());

                // Create the request
                final AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                                                                         .addAllFeatures(features)
                                                                         .setImage(img)
                                                                         .build();

                // Create the batch request with single image
                final BatchAnnotateImagesRequest batchRequest = BatchAnnotateImagesRequest.newBuilder()
                                                                                          .addRequests(request)
                                                                                          .build();

                // Call the Vision API
                final BatchAnnotateImagesResponse batchResponse = vision.batchAnnotateImages(batchRequest);
                final AnnotateImageResponse response = batchResponse.getResponses(0);

                // Process text detection results
                System.out.println("\nText Detections:");
                for (final EntityAnnotation text : response.getTextAnnotationsList()) {
                    System.out.printf("Text: %s%n", text.getDescription());
                    System.out.printf("Position: %s%n", text.getBoundingPoly());
                }

                // Process object detection results
                System.out.println("\nObject Detections:");
                for (final LocalizedObjectAnnotation object : response.getLocalizedObjectAnnotationsList()) {
                    System.out.printf("Object: %s%n", object.getName());
                    System.out.printf("Confidence: %.2f%n", object.getScore());
                    System.out.printf("Position: %s%n", object.getBoundingPoly());
                }

                if (response.hasError()) {
                    System.out.printf("Error: %s%n", response.getError().getMessage());
                    return;
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}