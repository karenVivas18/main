package com.follett.driver;

import java.util.HashMap;
import static java.time.Duration.ofSeconds;
import static org.openqa.selenium.UnexpectedAlertBehaviour.DISMISS;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.function.Supplier;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
/**
 * RemoteDriverBuilder represents the main interface for building remote drivers.
 */
public class RemoteDriverBuilder {

    private String remoteURL;
    private final Map<String, Supplier<AbstractDriverOptions<? extends AbstractDriverOptions>>> capabilityStrategy;
    private AbstractDriverOptions<? extends AbstractDriverOptions> capabilities;

    /**
     * Default constructor.
     */
    public RemoteDriverBuilder() {
        capabilityStrategy = new HashMap<>();
        capabilityStrategy.put("chrome", () -> {
            ChromeOptions options = new ChromeOptions();
            options
                    .setHeadless(true)
                    .addArguments("--disable-extensions")
                    .addArguments("--no-sandbox")
                    .addArguments("--ignore-certificate-errors")
                    .addArguments("enable-automation")
                    .addArguments("--window-size=1920,1200");
            return options;
        });
        capabilityStrategy.put("firefox", () -> {
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("-headless")
                    .addArguments("-width=1920")
                    .addArguments("-height=1200")
                    .setUnhandledPromptBehaviour(DISMISS);
            return options;
        });
        capabilityStrategy.put("iexplorer", () -> {
            InternetExplorerOptions options = new InternetExplorerOptions();
            options
                    .enablePersistentHovering()
                    .ignoreZoomSettings()
                    .introduceFlakinessByIgnoringSecurityDomains()
                    .requireWindowFocus()
                    .disableNativeEvents()
                    .withAttachTimeout(ofSeconds(45))
                    .merge(new DesiredCapabilities(
                            Map.of("ensureCleanSession", true,
                                    "unexpectedAlertBehaviour", "accept",
                                    "disable-popup-blocking", true)));
            return options;
        });
        capabilityStrategy.put("edge", EdgeOptions::new);
    }


    public WebDriver build() throws MalformedURLException {
        if ((remoteURL == null || remoteURL.isEmpty()) || capabilities == null) {
            throw new IllegalArgumentException("error");
        }
        RemoteWebDriver remoteWebDriver = new RemoteWebDriver(new URL(remoteURL), capabilities);
        remoteWebDriver.setFileDetector(new LocalFileDetector());
        return remoteWebDriver;
    }

    /**
     * Loads the desired capabilities from the configuration file.
     *
     * @param capabilityBrowser the capabilities.
     */
    public RemoteDriverBuilder loadCapabilities(String capabilityBrowser) {
        if (!capabilityStrategy.containsKey(capabilityBrowser)) {
            throw new IllegalArgumentException(String.format("this Browser %s is not supported", capabilityBrowser));
        }
        this.capabilities = capabilityStrategy.get(capabilityBrowser).get();
        return this;
    }

    /**
     * Sets the remote URL.
     *
     * @param remoteURL the URL as {@link String}
     */
    public RemoteDriverBuilder setRemoteURL(String remoteURL) {
        if (remoteURL == null || remoteURL.isEmpty()) {
            throw new WebDriverException("The remote URL is null or empty. Please provide a URL for the Selenium Remote Server.");
        }
        this.remoteURL = remoteURL;
        return this;
    }
}