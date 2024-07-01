package com.hyunn.commerceplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:spring-dotenv.properties")
public class CommercePlatformApplication {

  public static void main(String[] args) {
    SpringApplication.run(CommercePlatformApplication.class, args);
  }
}
