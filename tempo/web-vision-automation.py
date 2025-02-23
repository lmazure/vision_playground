import datetime
import os
from playwright.sync_api import sync_playwright
import torch
from PIL import Image
import torchvision.transforms as transforms
from transformers import CLIPProcessor, CLIPModel
import io

class VisionWebAutomator:
    def __init__(self):
        # Initialize CLIP model and processor
        self.model = CLIPModel.from_pretrained("openai/clip-vit-base-patch32")
        self.processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")
        
        # Initialize Playwright
        self.playwright = sync_playwright().start()
        self.browser = self.playwright.chromium.launch(headless=False)
        self.page = self.browser.new_page()
    
    def capture_screenshot(self):
        """Capture screenshot and convert to PIL Image"""
        screenshot_path = f"screenshots/{datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')}.png"
        self.page.screenshot(path=screenshot_path)
        screenshot_bytes = open(screenshot_path, 'rb').read()
        return Image.open(io.BytesIO(screenshot_bytes))
    
    # Function to encode text
    def encode_text(self, text):
        inputs = self.processor(text=text, return_tensors="pt", padding=True, truncation=True)
        with torch.no_grad():
            text_features = self.model.get_text_features(**inputs)
        return text_features
    
    # Function to encode image
    def encode_image(self, image_path):
        image = Image.open(image_path)
        inputs = self.processor(images=image, return_tensors="pt", padding=True, truncation=True)
        with torch.no_grad():
            image_features = self.model.get_image_features(**inputs)
        return image_features
    
    # Function to compute cosine similarity using PyTorch
    def compute_similarity(self, text_features, image_features):
        # Normalize the features
        text_features = text_features / text_features.norm(dim=-1, keepdim=True)
        image_features = image_features / image_features.norm(dim=-1, keepdim=True)
        # Compute cosine similarity
        similarity = (text_features @ image_features.T).squeeze()
        return similarity.item()
        
    def click_by_description(self, description):
        """Click an element based on visual description"""

        # Encode the text description
        text_features = self.encode_text(description)

        clickable = self.page.query_selector_all('button, a, [role="button"]')

        index = 0
        image_similarities = []

        for element in clickable:
            # Get element screenshot
            element_image = Image.open(
                io.BytesIO(element.screenshot())
            )
            # Write the image on the disk
            element_image_path = f"screenshots/element_{index}_{datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')}.png"
            element_image.save(element_image_path)
            
            image_features = self.encode_image(element_image_path)
            similarity_score = self.compute_similarity(text_features, image_features)
            image_similarities.append((element_image_path, similarity_score, element))

            index += 1
        
        image_similarities.sort(key=lambda x: x[1], reverse=True)

        # Print the top matching images
        top_n = 5  # Number of top matches to display
        print(f"Top {top_n} matching images for the description: '{description}'")
        for i, (image_name, similarity_score, element) in enumerate(image_similarities[:top_n]):
            print(f"{i+1}. {image_name} (Similarity: {similarity_score:.4f}) → {element})")

        # Click on the clickable element of the first entry of image_simlaities, wait and create a screenshot
        image_similarities[0][2].click()
        self.page.wait_for_timeout(1000)
        self.page.screenshot(path=f"screenshots/clicked_{datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')}.png")

        return False
    
    def navigate(self, url):
        """Navigate to a URL"""
        self.page.goto(url)
    
    def cleanup(self):
        """Clean up resources"""
        self.browser.close()
        self.playwright.stop()

def main():

    # Create a directory for screenshots if it doesn't exist
    screenshots_dir = "screenshots"
    if not os.path.exists(screenshots_dir):
        os.makedirs(screenshots_dir)

    automator = VisionWebAutomator()
    
    try:
        # Navigate to a website
        automator.navigate("https://mazure.fr")
        
        # Find and click a button using visual description
        automator.click_by_description(
            #"a magnifier glass icon"
            "a compass icon"
        )
 
        print("Couldn't find the described element")
            
    finally:
        automator.cleanup()

if __name__ == "__main__":
    main()