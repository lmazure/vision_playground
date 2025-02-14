package fr.mazure.vision;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.google.protobuf.ByteString;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.util.JsonFormat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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

        final Path screenshotPath = generateScreenshot(url);
        final String json = analyzeImageWithVision(screenshotPath);
        System.out.println(json);
    }
    
    public static Path generateScreenshot(final String url) {

        final Path screenshotPath = Paths.get("screenshots", "webpage_screenshot.png");

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
            page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath)
                                                        .setFullPage(true));

            // Clean up
            browser.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return screenshotPath;
    }

    private static String analyzeImageWithVision(final Path imagePath) throws IOException {
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
           /* // Process text detection results
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
            }*/
            if (response.hasError()) {
                throw new RuntimeException(response.getError().getMessage());
            }
            return JsonFormat.printer().includingDefaultValueFields().print(response);
        }
    }
}