package repository.jdbc;

import domain.Station;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import repository.interfaces.IStationRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class StationRepository implements IStationRepository {
    private static final Logger logger = LogManager.getLogger(StationRepository.class);
    private final DataSource dataSource;

    @Autowired
    public StationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public Station save(Station entity) {
        String sql = "INSERT INTO Stations (name) VALUES (?)";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setString(1, entity.getName());
            preStmt.executeUpdate();
            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: save Station", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public Optional<Station> findOne(Long id) {
        String sql = "SELECT * FROM Stations WHERE id = ?";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql)) {
            preStmt.setLong(1, id);
            try (ResultSet rs = preStmt.executeQuery()) {
                if (rs.next()) return Optional.of(new Station(id, rs.getString("name")));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findOne Station", ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Station> findByName(String name) {
        String sql = "SELECT * FROM Stations WHERE name = ?";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql)) {
            preStmt.setString(1, name);
            try (ResultSet rs = preStmt.executeQuery()) {
                if (rs.next()) return Optional.of(new Station(rs.getLong("id"), name));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findByName Station", ex);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Station> findAll() {
        ArrayList<Station> stations = new ArrayList<>();
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Stations");
             ResultSet rs = preStmt.executeQuery()) {
            while (rs.next()) {
                stations.add(new Station(rs.getLong("id"), rs.getString("name")));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findAll Stations", ex);
        }
        return stations;
    }

    @Override
    public void delete(Long id) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Stations WHERE id = ?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: delete Station", ex);
        }
    }

    @Override
    public Station update(Station entity) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("UPDATE Stations SET name = ? WHERE id = ?")) {
            preStmt.setString(1, entity.getName());
            preStmt.setLong(2, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: update Station", ex);
        }
        return entity;
    }
}