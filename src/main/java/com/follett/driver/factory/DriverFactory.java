package com.follett.driver.factory;

import com.follett.driver.DriverManager;
import com.follett.driver.RemoteDriverBuilder;
import org.springframework.stereotype.Component;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.springframework.beans.factory.annotation.Value;
import static java.nio.file.Files.createDirectories;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
@Component
@Slf4j
public class DriverFactory {
    private static final String DRIVER_FACTORY_VALIDATE = "DriverFactory::validate - ";

    private static final String CHROME = "chrome";
    private static final String FIREFOX = "firefox";
    private static final String IEXPLORER = "iexplorer";
    private static final String EDGE = "edge";

    @Value("${remoteUrl}")
    protected String remoteURL;

    @Value("${implicit.time.out}")
    private long implicitTimeout;

    @Value("${page.load.time.out}")
    private long pageLoadTimeout;

    @Value("${script.time.out}")
    private long scriptTimeout;


    /**
     * Returns a new instance of the particular web webDriver based on configuration.
     *
     * @param url     the base URL
     * @param browser the browser name
     * @param remote  to execute remotely or locally
     * @return the {@link WebDriver}
     * @throws IOException if the provider file does not exist or if it is invalid.
     */
    public WebDriver newInstance(String url, String browser, boolean remote) throws Exception {
        validate(browser);

        WebDriver driver;

        if (remote) {
            driver = newRemoteDriver(browser);
        } else {
            driver = newLocalDriver(browser);
            driver.manage().window().maximize();
        }

        driver.manage().timeouts().implicitlyWait(Duration.of(implicitTimeout, ChronoUnit.SECONDS));
        driver.manage().timeouts().pageLoadTimeout(Duration.of(pageLoadTimeout, ChronoUnit.SECONDS));
        driver.manage().timeouts().scriptTimeout(Duration.of(scriptTimeout, ChronoUnit.SECONDS));

        try {
            driver.get(url);
        } catch (Exception e) {
            log.error("Error when tried to start a new instance", e);
            driver.navigate().refresh();
        }
        return driver;
    }

    /**
     * Returns a new local webDriver based on configuration.
     *
     * @param browser the browser name
     * @return @return the {@link WebDriver}
     */
    private WebDriver newLocalDriver(String browser) {
        createDownloadsDirectory();

        return getWebDriverFromSupplier(browser);
    }

    /**
     * Returns a new remote webDriver based on configuration.
     *
     * @param browser the browser name
     * @return the {@link WebDriver}
     * @throws IOException if the provider file does not exist or if it is invalid.
     */
    private WebDriver newRemoteDriver(String browser) throws IOException {
        return new RemoteDriverBuilder().setRemoteURL(remoteURL).loadCapabilities(browser).build();
    }

    /**
     * Verifies if builders are available.
     *
     * @param browser the browser name
     */
    private void validate(String browser) {
        if (browser != null) {
            if (!browser.matches("^edge$|^chrome$|^iexplorer$|^firefox$")) {
                throw new IllegalArgumentException(
                        String.format(
                                "%s No local or remote webDriver builders are created for: %s",
                                DRIVER_FACTORY_VALIDATE,
                                browser
                        )
                );
            }
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "%s The browser is null. Please provide a valid browser [Chrome, Firefox, IExplorer, edge]",
                            DRIVER_FACTORY_VALIDATE
                    )
            );
        }
    }

    private String createDownloadsDirectory() {
        Path downloadPath = DriverManager.getDownloadDirector();
        Stream<Path> walk = null;
        try {
            if (downloadPath.toFile().exists()) {
                walk = Files.walk(downloadPath);
                walk.map(Path::toFile)
                        .filter(File::isFile)
                        .forEach(fileToDelete -> {
                            try {
                                FileDeleteStrategy.FORCE.delete(fileToDelete);
                            } catch (IOException e) {
                                log.error("Could not delete file: {}", e.getLocalizedMessage());
                            }
                        });
                Files.deleteIfExists(downloadPath);
            }
            return createDirectories(downloadPath).toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Could not delete & re-create downloads directory: " + e.getLocalizedMessage());
            return StringUtils.EMPTY;
        } finally {
            if (walk != null) walk.close();
        }
    }

  /*
  TODO review if this method should be used
  private void setWebDriverServersDownloadDirectory() {
    // Define WebDriver's driver download directory once!
    File tmpDir = new File("C:/Temp");
    if (!tmpDir.exists()) {
      try {
        Files.createDirectory(tmpDir.toPath());
      } catch (IOException e) {
        log.error(e.getLocalizedMessage(), e);
      }
    }
    System.setProperty("wdm.targetPath", tmpDir.getAbsolutePath());
  }*/


    private WebDriver getWebDriverFromSupplier(String browser) {
        Optional<Supplier<WebDriver>> supplierOptional = Optional.ofNullable(webDriverSupplier().get(browser));
        return supplierOptional.orElseThrow(() -> new IllegalArgumentException(
                String.format(
                        "%s No local or remote webDriver builders are created for: %s",
                        DRIVER_FACTORY_VALIDATE,
                        browser
                )
        )).get();
    }

    private Map<String, Supplier<WebDriver>> webDriverSupplier() {
        return Map.of(
                FIREFOX, () -> {
                    WebDriverManager.firefoxdriver().setup();
                    return new FirefoxDriver();
                },
                IEXPLORER, () -> {
                    WebDriverManager.iedriver().arch32().setup();
                    return new InternetExplorerDriver();
                },
                EDGE, () -> {
                    WebDriverManager.edgedriver().arch64().setup();
                    return new EdgeDriver();
                },
                CHROME, () -> {
                    WebDriverManager.chromedriver().setup();
                    return new ChromeDriver(new ChromeOptions().addArguments("--remote-allow-origins=*"));
                });
    }

}