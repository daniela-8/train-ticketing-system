package app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"config", "repository", "service", "controller"})
@EntityScan(basePackages = {"domain"})
@EnableJpaRepositories(basePackages = {"repository.interfaces"})
public class TicketingApplication {
    private static final Logger logger = LogManager.getLogger(TicketingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TicketingApplication.class, args);
        logger.info("Train Ticketing REST API (JPA Edition) is running!");
    }
}