package repository.jdbc;

import domain.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import repository.interfaces.IRouteRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class RouteRepository implements IRouteRepository {
    private static final Logger logger = LogManager.getLogger(RouteRepository.class);
    private final DataSource dataSource;

    @Autowired
    public RouteRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public Route save(Route entity) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("INSERT INTO Routes (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setString(1, entity.getName());
            preStmt.executeUpdate();
            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: save Route", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public Optional<Route> findOne(Long id) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Routes WHERE id=?")) {
            preStmt.setLong(1, id);
            try (ResultSet rs = preStmt.executeQuery()) {
                if (rs.next()) return Optional.of(new Route(id, rs.getString("name")));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findOne Route", ex);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Route> findAll() {
        ArrayList<Route> list = new ArrayList<>();
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Routes");
             ResultSet rs = preStmt.executeQuery()) {
            while (rs.next()) list.add(new Route(rs.getLong("id"), rs.getString("name")));
        } catch (SQLException ex) {
            logger.error("DB Error: findAll Routes", ex);
        }
        return list;
    }

    @Override
    public Route update(Route entity) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("UPDATE Routes SET name=? WHERE id=?")) {
            preStmt.setString(1, entity.getName());
            preStmt.setLong(2, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: update Route", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public void delete(Long id) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Routes WHERE id=?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: delete Route", ex);
        }
    }
}