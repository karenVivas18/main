package com.follett.config;


import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = SpringConfig.class,
        properties = "spring.main.allow-bean-definition-overriding=true")
public class CucumberSpringConfiguration {
}
