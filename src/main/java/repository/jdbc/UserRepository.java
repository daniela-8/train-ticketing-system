package repository.jdbc;

import domain.User;
import domain.enums.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import repository.interfaces.IUserRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class UserRepository implements IUserRepository {
    private static final Logger logger = LogManager.getLogger(UserRepository.class);
    private final DataSource dataSource;

    @Autowired
    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public User save(User entity) {
        String sql = "INSERT INTO Users (name, email, role) VALUES (?, ?, ?)";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setString(1, entity.getName());
            preStmt.setString(2, entity.getEmail());
            preStmt.setString(3, entity.getRole().name());
            preStmt.executeUpdate();
            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: save User", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public Optional<User> findOne(Long id) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Users WHERE id = ?")) {
            preStmt.setLong(1, id);
            try (ResultSet result = preStmt.executeQuery()) {
                if (result.next()) {
                    return Optional.of(new User(id, result.getString("name"), result.getString("email"), Role.valueOf(result.getString("role"))));
                }
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findOne User", ex);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<User> findAll() {
        ArrayList<User> users = new ArrayList<>();
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Users");
             ResultSet result = preStmt.executeQuery()) {
            while (result.next()) {
                users.add(new User(result.getLong("id"), result.getString("name"), result.getString("email"), Role.valueOf(result.getString("role"))));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findAll Users", ex);
        }
        return users;
    }

    @Override
    public User update(User entity) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("UPDATE Users SET name = ?, email = ?, role = ? WHERE id = ?")) {
            preStmt.setString(1, entity.getName());
            preStmt.setString(2, entity.getEmail());
            preStmt.setString(3, entity.getRole().name());
            preStmt.setLong(4, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: update User", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public void delete(Long id) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Users WHERE id = ?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: delete User", ex);
        }
    }
}