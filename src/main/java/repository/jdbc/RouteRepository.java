package repository.jdbc;

import domain.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import repository.interfaces.IRouteRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

@Repository
public class RouteRepository implements IRouteRepository {
    private static final Logger logger = LogManager.getLogger(RouteRepository.class);
    private JdbcUtils dbUtils;

    @Autowired
    public RouteRepository(Properties props) {
        dbUtils = new JdbcUtils(props);
    }

    @Override
    public Route save(Route entity) {
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement("INSERT INTO Routes (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setString(1, entity.getName());
            preStmt.executeUpdate();
            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) {
            logger.error("Database error occurred while saving Route", ex);
        }
        return entity;
    }

    @Override
    public Optional<Route> findOne(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Routes WHERE id=?")) {
            preStmt.setLong(1, id);
            try (ResultSet rs = preStmt.executeQuery()) {
                if (rs.next()) return Optional.of(new Route(id, rs.getString("name")));
            }
        } catch (SQLException ex) {
            logger.error("Database error occurred while finding Route by ID: {}", id, ex);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Route> findAll() {
        Connection con = dbUtils.getConnection();
        ArrayList<Route> list = new ArrayList<>();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Routes");
             ResultSet rs = preStmt.executeQuery()) {
            while (rs.next()) list.add(new Route(rs.getLong("id"), rs.getString("name")));
        } catch (SQLException ex) {
            logger.error("Database error occurred while finding all Routes", ex);
        }
        return list;
    }

    @Override
    public void delete(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Routes WHERE id=?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Database error occurred while deleting Route with ID: {}", id, ex);
        }
    }

    @Override
    public Route update(Route entity) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("UPDATE Routes SET name=? WHERE id=?")) {
            preStmt.setString(1, entity.getName());
            preStmt.setLong(2, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Database error occurred while updating Route with ID: {}", entity.getId(), ex);
        }
        return entity;
    }
}