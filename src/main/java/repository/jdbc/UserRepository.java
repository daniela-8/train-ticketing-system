package repository.jdbc;

import domain.User;
import domain.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import repository.interfaces.IUserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

@Repository
public class UserRepository implements IUserRepository {
    private JdbcUtils dbUtils;

    @Autowired
    public UserRepository(Properties props) {
        dbUtils = new JdbcUtils(props);
    }

    @Override
    public User save(User entity) {
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement(
                     "INSERT INTO Users (name, email, role) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)){

            preStmt.setString(1, entity.getName());
            preStmt.setString(2, entity.getEmail());
            preStmt.setString(3, entity.getRole().name());

            preStmt.executeUpdate();

            try (ResultSet generatedKeys = preStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error saving User to DB: " + ex);
        }
        return entity;
    }

    @Override
    public Optional<User> findOne(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Users WHERE id = ?")) {
            preStmt.setLong(1, id);
            try (ResultSet result = preStmt.executeQuery()) {
                if (result.next()) {
                    String name = result.getString("name");
                    String email = result.getString("email");
                    Role role = Role.valueOf(result.getString("role"));
                    return Optional.of(new User(id, name, email, role));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error finding User: " + ex);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<User> findAll() {
        Connection con = dbUtils.getConnection();
        ArrayList<User> users = new ArrayList<>();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Users");
             ResultSet result = preStmt.executeQuery()) {
            while (result.next()) {
                Long id = result.getLong("id");
                String name = result.getString("name");
                String email = result.getString("email");
                Role role = Role.valueOf(result.getString("role"));
                users.add(new User(id, name, email, role));
            }
        } catch (SQLException ex) {
            System.err.println("Error finding all Users: " + ex);
        }
        return users;
    }

    @Override
    public void delete(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Users WHERE id = ?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error deleting User: " + ex);
        }
    }

    @Override
    public User update(User entity) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(
                "UPDATE Users SET name = ?, email = ?, role = ? WHERE id = ?")) {
            preStmt.setString(1, entity.getName());
            preStmt.setString(2, entity.getEmail());
            preStmt.setString(3, entity.getRole().name());
            preStmt.setLong(4, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error updating User: " + ex);
        }
        return entity;
    }
}
