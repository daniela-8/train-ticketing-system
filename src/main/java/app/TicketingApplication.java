package app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"config", "repository", "service", "controller"})
public class TicketingApplication {
    private static final Logger logger = LogManager.getLogger(TicketingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TicketingApplication.class, args);
        logger.info("Train Ticketing REST API is running!");
    }
}