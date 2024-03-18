package com.follett.config;

import static com.github.automatedowl.tools.AllureEnvironmentWriter.allureEnvironmentWriter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import com.follett.driver.DriverManager;
import com.google.common.collect.ImmutableMap;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


public class ScenarioConfig {
    @Autowired
    private DriverManager driverManager;
    public static Scenario SCENARIO;
    @Value("${browser:API}")
    public String browser;
    @Value("${env}")
    public String environment;
    @Before(value = "@web", order = 1)
    public void beforeWebScenario() throws Exception {
        driverManager.populateDriver();
    }
    @After("@web")
    public void afterWebScenario() {
        if(SCENARIO.isFailed()){
            TakesScreenshot takesScreenshot = (TakesScreenshot)driverManager.getDriver().webDriver();
            final byte[] screenshot = takesScreenshot.getScreenshotAs(OutputType.BYTES);
            SCENARIO.attach(screenshot, "image/png", SCENARIO.getName());
        }
        driverManager.deleteDriver();
    }
    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        SCENARIO = scenario;
        allureEnvironmentWriter(
                ImmutableMap.of(
                        "Browser", browser,
                        "Environment", environment,
                        "Browser.Version", "87.0.4280.88"),
                System.getProperty("user.dir") + "/allure-reports/");
    }
    @BeforeAll
    void setAllureEnvironment() {
        // todo put the allureEnvironmentWriter here
    }
}
