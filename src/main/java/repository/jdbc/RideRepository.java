package repository.jdbc;

import domain.Ride;
import domain.RideSegment;
import domain.Route;
import domain.Train;
import repository.interfaces.IRideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

@Repository

public class RideRepository implements IRideRepository {
    private JdbcUtils dbUtils;
    @Autowired

    public RideRepository(Properties props) {
        dbUtils = new JdbcUtils(props);
    }

    @Override
    public Ride save(Ride entity) {
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement(
                "INSERT INTO Rides (train_id, route_id, delay_minutes) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setLong(1, entity.getTrain().getId());
            preStmt.setLong(2, entity.getRoute().getId());
            preStmt.setInt(3, entity.getDelayMinutes());
            preStmt.executeUpdate();
            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return entity;
    }

    @Override
    public Optional<Ride> findOne(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Rides WHERE id=?")) {
            preStmt.setLong(1, id);
            try (ResultSet rs = preStmt.executeQuery()) {
                if (rs.next()) {
                    Train shallowTrain = new Train(rs.getLong("train_id"), null, 0);
                    Route shallowRoute = new Route(rs.getLong("route_id"), null);
                    Ride ride = new Ride(id, shallowTrain, shallowRoute, new ArrayList<RideSegment>(), rs.getInt("delay_minutes"));
                    return Optional.of(ride);
                }
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return Optional.empty();
    }

    @Override
    public Iterable<Ride> findAll() {
        Connection con = dbUtils.getConnection();
        ArrayList<Ride> list = new ArrayList<>();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Rides");
             ResultSet rs = preStmt.executeQuery()) {
            while (rs.next()) {
                Train shallowTrain = new Train(rs.getLong("train_id"), null, 0);
                Route shallowRoute = new Route(rs.getLong("route_id"), null);
                list.add(new Ride(rs.getLong("id"), shallowTrain, shallowRoute, new ArrayList<RideSegment>(), rs.getInt("delay_minutes")));
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return list;
    }

    @Override
    public void delete(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Rides WHERE id=?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
    }

    @Override
    public Ride update(Ride entity) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(
                "UPDATE Rides SET train_id=?, route_id=?, delay_minutes=? WHERE id=?")) {
            preStmt.setLong(1, entity.getTrain().getId());
            preStmt.setLong(2, entity.getRoute().getId());
            preStmt.setInt(3, entity.getDelayMinutes());
            preStmt.setLong(4, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return entity;
    }
}