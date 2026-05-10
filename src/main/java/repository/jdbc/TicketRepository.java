package repository.jdbc;

import domain.*;
import domain.enums.TicketStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import repository.interfaces.ITicketRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class TicketRepository implements ITicketRepository {
    private static final Logger logger = LogManager.getLogger(TicketRepository.class);
    private final DataSource dataSource;

    @Autowired
    public TicketRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public Ticket save(Ticket entity) {
        String sql = "INSERT INTO Tickets (customer_id, ride_id, departure_station_id, arrival_station_id, number_of_seats, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (SQLException ex) {
            logger.error("DB Error: save Ticket", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public Iterable<Ticket> findAll() {
        ArrayList<Ticket> list = new ArrayList<>();
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Tickets");
             ResultSet rs = preStmt.executeQuery()) {
            while (rs.next()) {
                User shallowUser = new User(rs.getLong("customer_id"), null, null, null);
                Ride shallowRide = new Ride(rs.getLong("ride_id"), null, null, new ArrayList<>(), 0);
                Station shallowDep = new Station(rs.getLong("departure_station_id"), null);
                Station shallowArr = new Station(rs.getLong("arrival_station_id"), null);
                list.add(new Ticket(rs.getLong("id"), shallowUser, shallowRide, shallowDep, shallowArr,
                        rs.getInt("number_of_seats"), TicketStatus.valueOf(rs.getString("status"))));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findAll Tickets", ex);
        }
        return list;
    }

    @Override public Optional<Ticket> findOne(Long id) { return Optional.empty(); }
    @Override public void delete(Long id) {}
    @Override public Ticket update(Ticket entity) { return entity; }
}