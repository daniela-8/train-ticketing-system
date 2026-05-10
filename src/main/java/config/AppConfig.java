package config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableAsync
public class AppConfig {

    private static final Logger logger = LogManager.getLogger(AppConfig.class);

    @Bean
    public Properties dbProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) throw new RuntimeException("Cannot find db.properties");
            props.load(is);
            return props;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public DataSource dataSource(Properties dbProperties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbProperties.getProperty("jdbc.url"));
        config.setUsername(dbProperties.getProperty("jdbc.user"));
        config.setPassword(dbProperties.getProperty("jdbc.pass"));
        config.setMaximumPoolSize(10);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        logger.info("Enterprise Grade HikariCP DataSource initialized.");
        return new HikariDataSource(config);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}