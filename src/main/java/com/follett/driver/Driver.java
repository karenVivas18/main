package com.follett.driver;

import org.openqa.selenium.WebDriver;

public record Driver(String browser, WebDriver webDriver) {

    public static final String CHROME = "chrome";
    public static final String IEXPLORER = "iexplorer";
    public static final String FIREFOX = "firefox";

}
