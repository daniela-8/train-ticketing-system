package repository.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtils {
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
                System.out.println("HikariCP Connection Pool initialized successfully!");
            } catch (Exception e) {
                System.err.println("Error initializing connection pool: " + e.getMessage());
            }
        }
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Error getting connection from pool: " + e);
            return null;
        }
    }
}