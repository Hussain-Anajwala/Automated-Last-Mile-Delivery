package com.lastmile.selenium;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * When a journey fails, saves a screenshot, the page HTML and the URL/title/error to
 * {@code target/selenium-artifacts/<TestClass>/<method>.*}. Runs before {@code @AfterEach}, so the
 * browser is still open. Pages never contain password values, and test data is fictional.
 */
public class FailureArtifactsExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        Object instance = context.getRequiredTestInstance();
        if (!(instance instanceof BrowserJourney journey) || journey.driver() == null) {
            return;
        }
        WebDriver driver = journey.driver();
        Path dir = Path.of(System.getProperty("selenium.artifacts", "target/selenium-artifacts"),
                context.getRequiredTestClass().getSimpleName());
        String name = context.getRequiredTestMethod().getName();
        try {
            Files.createDirectories(dir);
            Path shot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(shot, dir.resolve(name + ".png"), StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(dir.resolve(name + ".html"), driver.getPageSource(), StandardCharsets.UTF_8);
            Files.writeString(dir.resolve(name + ".txt"),
                    "url: " + driver.getCurrentUrl() + System.lineSeparator()
                            + "title: " + driver.getTitle() + System.lineSeparator()
                            + "error: " + context.getExecutionException().get(), StandardCharsets.UTF_8);
            System.out.println("[selenium] failure artifacts saved to " + dir.toAbsolutePath().resolve(name + ".png"));
        } catch (IOException | RuntimeException ex) {
            System.out.println("[selenium] could not save failure artifacts: " + ex);
        }
    }
}
