package repository.jdbc;

import domain.RideSegment;
import domain.Station;
import repository.interfaces.IRideSegmentRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RideSegmentRepository implements IRideSegmentRepository {
    private JdbcUtils dbUtils;

    public RideSegmentRepository(Properties props) {
        dbUtils = new JdbcUtils(props);
    }

    @Override
    public RideSegment save(RideSegment entity) {
        return entity;
    }

    public RideSegment saveSegmentForRide(RideSegment entity, Long rideId) {
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement(
                     "INSERT INTO RideSegments (ride_id, from_station_id, to_station_id, available_seats) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            preStmt.setLong(1, rideId);
            preStmt.setLong(2, entity.getFromStation().getId());
            preStmt.setLong(3, entity.getToStation().getId());
            preStmt.setInt(4, entity.getAvailableSeats());
            preStmt.executeUpdate();

            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return entity;
    }

    @Override
    public Optional<RideSegment> findOne(Long id) {
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement("SELECT * FROM RideSegments WHERE id=?")) {
            preStmt.setLong(1, id);
            try (ResultSet rs = preStmt.executeQuery()) {
                if (rs.next()) {
                    Station from = new Station(rs.getLong("from_station_id"), null);
                    Station to = new Station(rs.getLong("to_station_id"), null);
                    return Optional.of(new RideSegment(id, from, to, rs.getInt("available_seats")));
                }
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return Optional.empty();
    }

    @Override
    public Iterable<RideSegment> findAll() {
        return new ArrayList<>();
    }

    @Override
    public List<RideSegment> findByRideId(Long rideId) {
        List<RideSegment> list = new ArrayList<>();
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement("SELECT * FROM RideSegments WHERE ride_id=? ORDER BY id ASC")) {
            preStmt.setLong(1, rideId);
            try (ResultSet rs = preStmt.executeQuery()) {
                while (rs.next()) {
                    Station from = new Station(rs.getLong("from_station_id"), null);
                    Station to = new Station(rs.getLong("to_station_id"), null);
                    list.add(new RideSegment(rs.getLong("id"), from, to, rs.getInt("available_seats")));
                }
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return list;
    }

    @Override
    public void delete(Long id) {
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement("DELETE FROM RideSegments WHERE id=?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
    }

    @Override
    public RideSegment update(RideSegment entity) {
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement(
                     "UPDATE RideSegments SET available_seats=? WHERE id=?")) {
            preStmt.setInt(1, entity.getAvailableSeats());
            preStmt.setLong(2, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return entity;
    }
}