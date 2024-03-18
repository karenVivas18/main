package com.follett.driver;

import com.follett.driver.factory.DriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
/**
 * DriverManager manages the web webDriver instantiation based on the configuration properties.
 * <p/>
 * DriverManager delegates the webDriver building process to {@link DriverFactory}.
 * <p/>
 * DriverManager supports running test in parallel by storing the drivers created in an array of {@link
 * InheritableThreadLocal}. By doing this, it ensures  that every thread (test method under execution) will receive a
 * new instance of the particular webDriver.
 */
@Component
@Slf4j
public class DriverManager {

    private final InheritableThreadLocal<Driver> driverInheritableThreadLocal = new InheritableThreadLocal<>();

    @Value("${ns.web.cas.url}")
    private String url;

    @Value("${browser}")
    private String browser;

    @Value("${remote}")
    private boolean remote;

    @Autowired
    private DriverFactory driverFactory;

    private boolean cleanSession = false;

    /**
     * Populates the webDriver for the current test method (thread).
     *
     * @throws IOException if the provider file does not exist or if it is invalid.
     */
    public void populateDriver() throws Exception {
        WebDriver driver = driverFactory.newInstance(url, browser, remote);
        if (isCleanSession()) {
            driver.manage().deleteAllCookies();
        }
        driverInheritableThreadLocal.set(new Driver(browser, driver));
    }

    /**
     * Gets the webDriver for the current test execution.
     *
     * @return the {@link WebDriver}
     */
    public Driver getDriver() {
        return driverInheritableThreadLocal.get();
    }

    /**
     * Deletes the webDriver for the current test execution.
     */
    public void deleteDriver() {
        Driver driver = getDriver();
        if (driver != null && driver.webDriver() != null) {
            driverInheritableThreadLocal.remove();
            try {
                driver.webDriver().quit();
            } catch (Exception e) {
                log.error("DriverManager:deleteDriver - Unable to close all browsers instances - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Refreshes the webDriver for the current test execution.
     */
    public void refreshDriver() {
        WebDriver driver = getDriver().webDriver();
        if (driver != null) {
            Set<String> windowHandles = driver.getWindowHandles();
            if (windowHandles.size() > 1) {
                Iterator<String> iterator = windowHandles.iterator();
                iterator.next();
                while (iterator.hasNext()) {
                    String windowHandle = iterator.next();
                    driver.switchTo().window(windowHandle);
                    driver.close();
                }
            }
            driver.switchTo().defaultContent();
            driver.manage().deleteAllCookies();
            driver.get(url);
        }
    }

    /**
     * Gets an auxiliary web webDriver instance not attached to {@link DriverManager}.
     *
     * @return the {@link WebDriver}
     * @throws IOException if the provider file does not exist or if it is invalid.
     */
    public WebDriver getAuxiliaryWebDriverInstance() throws Exception {
        return getAuxiliaryWebDriverInstance(url);
    }

    /**
     * Gets an auxiliary web webDriver instance not attached to {@link DriverManager}.
     *
     * @param url the base url
     * @return the {@link WebDriver}
     * @throws IOException if the provider file does not exist or if it is invalid.
     */
    public WebDriver getAuxiliaryWebDriverInstance(String url) throws Exception {
        WebDriver driver = driverFactory.newInstance(url, browser, remote);
        if (isCleanSession()) {
            driver.manage().deleteAllCookies();
        }
        return driver;
    }

    public static Path getDownloadDirector() {
        return Paths.get("downloads");
    }

    public DriverFactory getDriverFactory() {
        return driverFactory;
    }

    public void setDriverFactory(DriverFactory driverFactory) {
        this.driverFactory = driverFactory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }
}