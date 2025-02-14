package fr.mazure.vision;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.util.JsonFormat;


public class GoogleVision {

    public static String analyzeImageWithVision(final Path imagePath) throws IOException {
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

            if (response.hasError()) {
                throw new RuntimeException(response.getError().getMessage());
            }
            return JsonFormat.printer().includingDefaultValueFields().print(response);
        }
    }

}
