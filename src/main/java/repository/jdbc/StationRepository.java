package repository.jdbc;

import domain.Station;
import repository.interfaces.IStationRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

public class StationRepository implements IStationRepository {
    private JdbcUtils dbUtils;

    public StationRepository(Properties props) {
        dbUtils = new JdbcUtils(props);
    }

    @Override
    public Station save(Station entity) {
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement(
                "INSERT INTO Stations (name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {

            preStmt.setString(1, entity.getName());
            preStmt.executeUpdate();

            try (ResultSet generatedKeys = preStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error saving Station: " + ex);
        }
        return entity;
    }

    @Override
    public Optional<Station> findOne(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Stations WHERE id = ?")) {
            preStmt.setLong(1, id);
            try (ResultSet result = preStmt.executeQuery()) {
                if (result.next()) {
                    return Optional.of(new Station(id, result.getString("name")));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error finding Station: " + ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Station> findByName(String name) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Stations WHERE name = ?")) {
            preStmt.setString(1, name);
            try (ResultSet result = preStmt.executeQuery()) {
                if (result.next()) {
                    return Optional.of(new Station(result.getLong("id"), name));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error finding Station by name: " + ex);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Station> findAll() {
        Connection con = dbUtils.getConnection();
        ArrayList<Station> stations = new ArrayList<>();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Stations");
             ResultSet result = preStmt.executeQuery()) {
            while (result.next()) {
                stations.add(new Station(result.getLong("id"), result.getString("name")));
            }
        } catch (SQLException ex) {
            System.err.println("Error finding all Stations: " + ex);
        }
        return stations;
    }

    @Override
    public void delete(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Stations WHERE id = ?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error deleting Station: " + ex);
        }
    }

    @Override
    public Station update(Station entity) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("UPDATE Stations SET name = ? WHERE id = ?")) {
            preStmt.setString(1, entity.getName());
            preStmt.setLong(2, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error updating Station: " + ex);
        }
        return entity;
    }
}