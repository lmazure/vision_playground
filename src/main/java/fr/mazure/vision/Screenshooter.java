package fr.mazure.vision;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

/**
 * The Screenshooter class provides methods to take screenshots of web pages.
 */
public class Screenshooter {
    private static int incr = 0;

    /**
     * Returns the path of the screenshot with the given id.
     * The screenshot is stored in the "screenshots" directory and has a name
     * of the form "webpage_screenshot{id}.png", where {id} is replaced with the
     * given id.
     * @param id the id of the screenshot to retrieve
     * @return the path of the screenshot
     */
    public static Path getScreenshotPath(final int id) {
        return Paths.get("screenshots", "webpage_screenshot" + id + ".png");
    }
    
    /**
     * Generates a screenshot of the given web page.
     * The method returns the id of the screenshot generated.
     * @param url the URL of the web page to generate a screenshot of
     * @return the id of the screenshot generated
     */
    public static int generateScreenshot(final String url) {

        final int id = incr++;
        final Path screenshotPath = getScreenshotPath(id);

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

        return id;
    }
}