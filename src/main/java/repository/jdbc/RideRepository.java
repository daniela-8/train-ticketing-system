package repository.jdbc;

import domain.Ride;
import domain.RideSegment;
import domain.Route;
import domain.Train;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import repository.interfaces.IRideRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class RideRepository implements IRideRepository {
    private static final Logger logger = LogManager.getLogger(RideRepository.class);
    private final DataSource dataSource;

    @Autowired
    public RideRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public Ride save(Ride entity) {
        String sql = "INSERT INTO Rides (train_id, route_id, delay_minutes) VALUES (?, ?, ?)";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setLong(1, entity.getTrain().getId());
            preStmt.setLong(2, entity.getRoute().getId());
            preStmt.setInt(3, entity.getDelayMinutes());
            preStmt.executeUpdate();
            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: save Ride", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public Optional<Ride> findOne(Long id) {
        String sql = "SELECT * FROM Rides WHERE id=?";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql)) {
            preStmt.setLong(1, id);
            try (ResultSet rs = preStmt.executeQuery()) {
                if (rs.next()) {
                    Train shallowTrain = new Train(rs.getLong("train_id"), null, 0);
                    Route shallowRoute = new Route(rs.getLong("route_id"), null);
                    return Optional.of(new Ride(id, shallowTrain, shallowRoute, new ArrayList<>(), rs.getInt("delay_minutes")));
                }
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findOne Ride", ex);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Ride> findAll() {
        ArrayList<Ride> list = new ArrayList<>();
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Rides");
             ResultSet rs = preStmt.executeQuery()) {
            while (rs.next()) {
                Train shallowTrain = new Train(rs.getLong("train_id"), null, 0);
                Route shallowRoute = new Route(rs.getLong("route_id"), null);
                list.add(new Ride(rs.getLong("id"), shallowTrain, shallowRoute, new ArrayList<>(), rs.getInt("delay_minutes")));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findAll Rides", ex);
        }
        return list;
    }

    @Override
    public Ride update(Ride entity) {
        String sql = "UPDATE Rides SET train_id=?, route_id=?, delay_minutes=? WHERE id=?";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql)) {
            preStmt.setLong(1, entity.getTrain().getId());
            preStmt.setLong(2, entity.getRoute().getId());
            preStmt.setInt(3, entity.getDelayMinutes());
            preStmt.setLong(4, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: update Ride", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public void delete(Long id) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Rides WHERE id=?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: delete Ride", ex);
        }
    }
}