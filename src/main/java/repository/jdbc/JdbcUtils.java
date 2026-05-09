package repository.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtils {
    private Properties jdbcProps;
    private Connection instance = null;

    public JdbcUtils(Properties props) {
        this.jdbcProps = props;
    }

    private Connection getNewConnection() {
        String url = jdbcProps.getProperty("jdbc.url");
        String user = jdbcProps.getProperty("jdbc.user");
        String pass = jdbcProps.getProperty("jdbc.pass");
        Connection con = null;
        try {
            con = DriverManager.getConnection(url, user, pass);
            System.out.println("Successfully connected to MySQL database.");
        } catch (SQLException e) {
            System.err.println("Error getting connection: " + e);
        }
        return con;
    }

    public Connection getConnection() {
        try {
            if (instance == null || instance.isClosed() || !instance.isValid(5)) {
                instance = getNewConnection();
            }
        } catch (SQLException e) {
            System.err.println("Error DB " + e);
        }
        return instance;
    }
}
