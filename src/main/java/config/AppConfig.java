package config;

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

    @Primary
    @Bean
    public Properties dbProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException("Cannot find db.properties in resources folder!");
            }
            props.load(is);
            System.out.println("Spring Bean: Loaded db.properties successfully!");
        } catch (IOException e) {
            throw new RuntimeException("Error loading db.properties", e);
        }
        return props;
    }
}