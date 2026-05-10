package repository.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtils {
    private static final Logger logger = LogManager.getLogger(JdbcUtils.class);
    private Properties jdbcProps;
    private static HikariDataSource dataSource;

    public JdbcUtils(Properties props) {
        this.jdbcProps = props;
        initializePool();
    }

    private void initializePool() {
        if (dataSource == null) {
            try {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcProps.getProperty("jdbc.url"));
                config.setUsername(jdbcProps.getProperty("jdbc.user"));
                config.setPassword(jdbcProps.getProperty("jdbc.pass"));

                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(30000);

                dataSource = new HikariDataSource(config);
                logger.info("HikariCP Connection Pool initialized successfully!");
            } catch (Exception e) {
                logger.fatal("Error initializing connection pool: {}", e.getMessage(), e);
            }
        }
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Error getting connection from pool", e);
            return null;
        }
    }
}