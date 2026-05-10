package repository.jdbc;

import domain.*;
import domain.enums.TicketStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import repository.interfaces.ITicketRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

@Repository
public class TicketRepository implements ITicketRepository {
    private JdbcUtils dbUtils;

    @Autowired
    public TicketRepository(Properties props) {
        dbUtils = new JdbcUtils(props);
    }

    @Override
    public Ticket save(Ticket entity) {
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement(
                "INSERT INTO Tickets (customer_id, ride_id, departure_station_id, arrival_station_id, number_of_seats, status) VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setLong(1, entity.getCustomer().getId());
            preStmt.setLong(2, entity.getRide().getId());
            preStmt.setLong(3, entity.getDepartureStation().getId());
            preStmt.setLong(4, entity.getArrivalStation().getId());
            preStmt.setInt(5, entity.getNumberOfSeats());

            preStmt.setString(6, entity.getStatus().name());

            preStmt.executeUpdate();
            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return entity;
    }

    @Override
    public Optional<Ticket> findOne(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Tickets WHERE id=?")) {
            preStmt.setLong(1, id);
            try (ResultSet rs = preStmt.executeQuery()) {
                if (rs.next()) {
                    User shallowUser = new User(rs.getLong("customer_id"), null, null, null);
                    Ride shallowRide = new Ride(rs.getLong("ride_id"), null, null, new ArrayList<RideSegment>(), 0);
                    Station shallowDep = new Station(rs.getLong("departure_station_id"), null);
                    Station shallowArr = new Station(rs.getLong("arrival_station_id"), null);

                    Ticket ticket = new Ticket(id, shallowUser, shallowRide, shallowDep, shallowArr,
                            rs.getInt("number_of_seats"), TicketStatus.valueOf(rs.getString("status")));
                    return Optional.of(ticket);
                }
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return Optional.empty();
    }

    @Override
    public Iterable<Ticket> findAll() {
        Connection con = dbUtils.getConnection();
        ArrayList<Ticket> list = new ArrayList<>();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Tickets");
             ResultSet rs = preStmt.executeQuery()) {
            while (rs.next()) {
                User shallowUser = new User(rs.getLong("customer_id"), null, null, null);
                Ride shallowRide = new Ride(rs.getLong("ride_id"), null, null, new ArrayList<RideSegment>(), 0);
                Station shallowDep = new Station(rs.getLong("departure_station_id"), null);
                Station shallowArr = new Station(rs.getLong("arrival_station_id"), null);

                list.add(new Ticket(rs.getLong("id"), shallowUser, shallowRide, shallowDep, shallowArr,
                        rs.getInt("number_of_seats"), TicketStatus.valueOf(rs.getString("status"))));
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return list;
    }

    @Override
    public void delete(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Tickets WHERE id=?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
    }

    @Override
    public Ticket update(Ticket entity) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(
                "UPDATE Tickets SET customer_id=?, ride_id=?, departure_station_id=?, arrival_station_id=?, number_of_seats=?, status=? WHERE id=?")) {
            preStmt.setLong(1, entity.getCustomer().getId());
            preStmt.setLong(2, entity.getRide().getId());
            preStmt.setLong(3, entity.getDepartureStation().getId());
            preStmt.setLong(4, entity.getArrivalStation().getId());
            preStmt.setInt(5, entity.getNumberOfSeats());

            preStmt.setString(6, entity.getStatus().name());

            preStmt.setLong(7, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return entity;
    }
}