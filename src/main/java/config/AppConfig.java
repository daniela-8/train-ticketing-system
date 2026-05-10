package config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = {"repository", "service"})
public class AppConfig {

    private static final Logger logger = LogManager.getLogger(AppConfig.class);

    @Primary
    @Bean
    public Properties dbProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                logger.fatal("Cannot find db.properties in resources folder!");
                throw new RuntimeException("Cannot find db.properties in resources folder!");
            }
            props.load(is);
            logger.info("Spring Bean: Loaded db.properties successfully!");
        } catch (IOException e) {
            logger.error("Error loading db.properties", e);
            throw new RuntimeException("Error loading db.properties", e);
        }
        return props;
    }
}