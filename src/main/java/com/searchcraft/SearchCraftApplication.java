package com.searchcraft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SearchCraft Application Entry Point
 *
 * This is the bootstrap class for our mini search engine.
 * @SpringBootApplication enables:
 * 1. @Configuration: Tags the class as a source of bean definitions.
 * 2. @EnableAutoConfiguration: Tells Spring Boot to configure beans based on classpath settings.
 * 3. @ComponentScan: Scans com.searchcraft and its sub-packages for @Component, @Service, @Repository, and @RestController.
 */
@SpringBootApplication
public class SearchCraftApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchCraftApplication.class, args);
    }
}
