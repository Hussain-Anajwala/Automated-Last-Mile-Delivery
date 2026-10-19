package com.lastmile.selenium;

import java.io.File;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Creates the browser for journeys. Settings come from system properties (set by the
 * {@code selenium} Maven profile) so the same tests run headless on a laptop and in Jenkins:
 * <ul>
 *   <li>{@code selenium.headless} (default true)</li>
 *   <li>{@code selenium.chrome.binary} — path to Chrome/Chromium when not on the default path</li>
 *   <li>{@code selenium.noSandbox} — needed for Chromium inside some containers</li>
 *   <li>{@code selenium.chromedriver} — explicit driver path (e.g. Debian's {@code /usr/bin/chromedriver});
 *       otherwise Selenium Manager resolves a matching driver</li>
 * </ul>
 */
final class BrowserFactory {

    private BrowserFactory() {
    }

    static WebDriver chrome() {
        String driverPath = System.getProperty("selenium.chromedriver", "");
        ChromeDriverService service = driverPath.isBlank()
                ? ChromeDriverService.createDefaultService()
                : new ChromeDriverService.Builder().usingDriverExecutable(new File(driverPath)).build();
        return new ChromeDriver(service, options());
    }

    private static ChromeOptions options() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("selenium.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1366,900", "--disable-dev-shm-usage", "--disable-gpu",
                "--disable-search-engine-choice-screen", "--no-first-run");
        String binary = System.getProperty("selenium.chrome.binary", "");
        if (!binary.isBlank()) {
            options.setBinary(binary);
        }
        if (Boolean.parseBoolean(System.getProperty("selenium.noSandbox", "false"))) {
            options.addArguments("--no-sandbox");
        }
        return options;
    }
}
