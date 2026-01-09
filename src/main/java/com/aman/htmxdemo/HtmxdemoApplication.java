package com.aman.htmxdemo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class HtmxdemoApplication {

 static void main(String[] args) {
     Dotenv dotenv = Dotenv.load();
     System.setProperty("HTMX_DB_URL", Objects.requireNonNull(dotenv.get("HTMX_DB_URL")));
     System.setProperty("HTMX_DB_USERNAME", Objects.requireNonNull(dotenv.get("HTMX_DB_USERNAME")));
     System.setProperty("HTMX_DB_PASSWORD", Objects.requireNonNull(dotenv.get("HTMX_DB_PASSWORD")));
        SpringApplication.run(HtmxdemoApplication.class, args);
	}

}
