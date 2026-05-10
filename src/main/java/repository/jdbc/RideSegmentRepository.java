package repository.jdbc;

import domain.RideSegment;
import domain.Station;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import repository.interfaces.IRideSegmentRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RideSegmentRepository implements IRideSegmentRepository {
    private static final Logger logger = LogManager.getLogger(RideSegmentRepository.class);
    private final DataSource dataSource;

    @Autowired
    public RideSegmentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public RideSegment save(RideSegment entity) { return entity; }

    public RideSegment saveSegmentForRide(RideSegment entity, Long rideId) {
        String sql = "INSERT INTO RideSegments (ride_id, from_station_id, to_station_id, available_seats, departure_time, arrival_time) VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setLong(1, rideId);
            preStmt.setLong(2, entity.getFromStation().getId());
            preStmt.setLong(3, entity.getToStation().getId());
            preStmt.setInt(4, entity.getAvailableSeats());
            preStmt.setObject(5, entity.getDepartureTime());
            preStmt.setObject(6, entity.getArrivalTime());
            preStmt.executeUpdate();
            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: saveSegmentForRide", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public List<RideSegment> findByRideId(Long rideId) {
        List<RideSegment> list = new ArrayList<>();
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM RideSegments WHERE ride_id=? ORDER BY id ASC")) {
            preStmt.setLong(1, rideId);
            try (ResultSet rs = preStmt.executeQuery()) {
                while (rs.next()) {
                    Station from = new Station(rs.getLong("from_station_id"), null);
                    Station to = new Station(rs.getLong("to_station_id"), null);
                    LocalDateTime depTime = rs.getObject("departure_time", LocalDateTime.class);
                    LocalDateTime arrTime = rs.getObject("arrival_time", LocalDateTime.class);
                    list.add(new RideSegment(rs.getLong("id"), from, to, depTime, arrTime, rs.getInt("available_seats")));
                }
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findByRideId", ex);
        }
        return list;
    }

    @Override
    public RideSegment update(RideSegment entity) {
        String sql = "UPDATE RideSegments SET available_seats=?, departure_time=?, arrival_time=? WHERE id=?";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql)) {
            preStmt.setInt(1, entity.getAvailableSeats());
            preStmt.setObject(2, entity.getDepartureTime());
            preStmt.setObject(3, entity.getArrivalTime());
            preStmt.setLong(4, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: update RideSegment", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override public Optional<RideSegment> findOne(Long id) { return Optional.empty(); }
    @Override public Iterable<RideSegment> findAll() { return new ArrayList<>(); }
    @Override public void delete(Long id) {}
}