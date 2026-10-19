package com.lastmile.selenium;

import java.time.Duration;
import java.util.List;

import com.lastmile.support.EmbeddedPostgresInitializer;
import com.lastmile.support.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * Base for browser journeys: the real application on a random port against embedded PostgreSQL,
 * a fresh browser per test, fresh fictional data per test, explicit waits only (no sleeps) and
 * {@code data-testid} locators only.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = EmbeddedPostgresInitializer.class)
@Import(TestData.class)
@ExtendWith(FailureArtifactsExtension.class)
public abstract class BrowserJourney {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @LocalServerPort
    int port;

    @Autowired
    protected TestData data;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void startBrowser() {
        data.reset();
        driver = BrowserFactory.chrome();
        wait = new WebDriverWait(driver, TIMEOUT);
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    WebDriver driver() {
        return driver;
    }

    protected static By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    protected void open(String path) {
        driver.get("http://localhost:" + port + path);
    }

    protected void login(String email) {
        open("/login");
        type("login-email", email);
        type("login-password", TestData.PASSWORD);
        click("login-submit");
        waitForUrl("/dashboard");
    }

    protected void logout() {
        click("logout");
        waitFor("logout-message");
    }

    protected WebElement waitFor(String id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(testId(id)));
    }

    protected void waitForUrl(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    protected String text(String id) {
        return waitFor(id).getText().trim();
    }

    /** Elements on the current page; callers navigate with {@link #click} or {@link #open} first. */
    protected List<WebElement> all(String id) {
        return driver.findElements(testId(id));
    }

    protected void type(String id, String value) {
        WebElement field = waitFor(id);
        field.clear();
        field.sendKeys(value);
    }

    protected void select(String id, String visibleText) {
        new Select(waitFor(id)).selectByVisibleText(visibleText);
    }

    /**
     * Clicks a link or submit button and waits for the next page. Two details matter:
     * <ul>
     *   <li>Scrolls instantly first: Bootstrap enables smooth scrolling, which otherwise races
     *       WebDriver's own scroll and intercepts clicks on controls below the fold.</li>
     *   <li>Waits until a new document has fully loaded, so assertions never read a flash message
     *       or status badge left over from the previous page. The old page is detected by a marker
     *       on its {@code window} object rather than by polling an old DOM node: during redirects
     *       Chrome may answer queries about old nodes with a generic error instead of "stale"
     *       (observed in run 3 of the Phase 9 repeatability check).</li>
     * </ul>
     * Every click in these journeys navigates (links and server-validated forms).
     */
    protected void click(String id) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(testId(id)));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.__lmPreviousPage = true;"
                + "arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});", element);
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        // The probe can throw transiently while the old document is being torn down; keep polling.
        new WebDriverWait(driver, TIMEOUT).ignoring(WebDriverException.class)
                .until(d -> Boolean.TRUE.equals(((JavascriptExecutor) d).executeScript(
                        "return window.__lmPreviousPage === undefined && document.readyState === 'complete';")));
    }

    protected String currentPath() {
        return driver.getCurrentUrl().replaceFirst("^https?://[^/]+", "");
    }

    protected String statusBadge() {
        return waitFor("status-badge").getAttribute("data-status");
    }
}
