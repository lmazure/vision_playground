package fr.mazure.vision;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api")
public class WebVisionController {

    @PostMapping("/load_image")
    public ResponseEntity<String> loadImage(@RequestParam String url) {
        try {
            final int id = Screenshooter.generateScreenshot(url);
            final String json = "{\"id\": " + id + "}";
            return ResponseEntity.ok(json);
        } catch (final Exception e) {
            return ResponseEntity.internalServerError().body(buildErrorJson("Error generatin screenshot", e));
        }
    }

    @GetMapping(value = "/get_image/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getImage(@PathVariable int id) {
        try {
            final Path imagePath = Screenshooter.getScreenshotPath(id);
            final Resource resource = new FileSystemResource(imagePath);
            
            if (resource.exists()) {
                return ResponseEntity.ok()
                                     .contentType(MediaType.IMAGE_PNG)
                                     .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (final Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/get_annotation/{id}")
    public ResponseEntity<String> getAnnotation(@PathVariable int id) {
        try {
            final Path imagePath = Screenshooter.getScreenshotPath(id);
            final String json = GoogleVision.analyzeImageWithVision(imagePath);
            return ResponseEntity.ok(json);
        } catch (final AiException e) {
            return ResponseEntity.internalServerError().body(buildErrorJson("Error calling AI", e));
        } catch (final IOException e) {
            return ResponseEntity.internalServerError().body(buildErrorJson("Error reading image", e));
        }
    }

    private static String buildErrorJson(final String prefix, final Exception e) {
        String message = e.getMessage();
        message = message.replace("\\", "\\\\");
        message = message.replace("\"", "\\\"");
        return "{\"error\": \"" + prefix + ": " + message + "\"}";
    }
}
