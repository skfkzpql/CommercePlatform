package com.hyunn.commerceplatform;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommercePlatformApplication {

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.load();

    dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

    SpringApplication.run(CommercePlatformApplication.class, args);
  }
}
