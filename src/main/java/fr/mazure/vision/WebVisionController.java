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
            final String json = "{\"error\": \"" + e.getMessage() + "\"}";  
            return ResponseEntity.internalServerError().body(json);
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
        System.out.println("get_annotation " + id);
        try {
            final Path imagePath = Screenshooter.getScreenshotPath(id);
            System.out.println(imagePath);
            final String json = GoogleVision.analyzeImageWithVision(imagePath);
            System.out.println(json);

            return ResponseEntity.ok(json);
        } catch (final AiException e) {
            return ResponseEntity.internalServerError().body("Error calling AI: " + e.getMessage());
        } catch (final IOException e) {
            return ResponseEntity.internalServerError().body("Error reading image: " + e.getMessage());
        }
    }
}
