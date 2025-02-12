package fr.mazure.vision;

import com.microsoft.playwright.*;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.*;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class WebVisionAnalyzer {
    public static void main(String[] args) {
        String url = "https://example.com";  // Replace with your target URL
        analyzeWebPage(url);
    }

    public static void analyzeWebPage(String url) {
        try (Playwright playwright = Playwright.create()) {
            // Launch browser in headless mode
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(true)
            );

            // Create a new browser context
            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080)
            );

            // Create a new page
            Page page = context.newPage();

            // Navigate to the URL
            page.navigate(url);
            
            // Wait for the network to be idle
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Take screenshot
            Path screenshotPath = Paths.get("webpage_screenshot.png");
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(screenshotPath)
                .setFullPage(true));

            // Analyze the screenshot with Google Cloud Vision
            analyzeImageWithVision(screenshotPath);

            // Clean up
            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyzeImageWithVision(Path imagePath) {
        try {
            // Initialize the client with TCP transport configuration
            /*ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setTransportChannelProvider(
                    InstantiatingGrpcChannelProvider.newBuilder()
                        .setEndpoint("vision.googleapis.com:443")
                        .setChannelConfigurator(managedChannelBuilder -> 
                            managedChannelBuilder.usePlaintext())
                        .build())
                .build();*/

            try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
                // Read the image file
                byte[] imageBytes = Files.readAllBytes(imagePath);
                ByteString imgBytes = ByteString.copyFrom(imageBytes);

                // Create image object
                Image img = Image.newBuilder().setContent(imgBytes).build();

                // Create feature list for different types of detection
                List<Feature> features = new ArrayList<>();
                
                // Add text detection
                features.add(Feature.newBuilder().setType(Type.TEXT_DETECTION).build());
                
                // Add object detection (can help identify buttons and UI elements)
                features.add(Feature.newBuilder().setType(Type.OBJECT_LOCALIZATION).build());
                
                // Create the request
                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addAllFeatures(features)
                    .setImage(img)
                    .build();

                // Create the batch request with single image
                BatchAnnotateImagesRequest batchRequest = BatchAnnotateImagesRequest.newBuilder()
                    .addRequests(request)
                    .build();

                // Call the Vision API
                BatchAnnotateImagesResponse batchResponse = vision.batchAnnotateImages(batchRequest);
                AnnotateImageResponse response = batchResponse.getResponses(0);

                // Process text detection results
                System.out.println("\nText Detections:");
                for (EntityAnnotation text : response.getTextAnnotationsList()) {
                    System.out.printf("Text: %s%n", text.getDescription());
                    System.out.printf("Position: %s%n", text.getBoundingPoly());
                }

                // Process object detection results
                System.out.println("\nObject Detections:");
                for (LocalizedObjectAnnotation object : response.getLocalizedObjectAnnotationsList()) {
                    System.out.printf("Object: %s%n", object.getName());
                    System.out.printf("Confidence: %.2f%n", object.getScore());
                    System.out.printf("Position: %s%n", object.getBoundingPoly());
                }

                if (response.hasError()) {
                    System.out.printf("Error: %s%n", response.getError().getMessage());
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}