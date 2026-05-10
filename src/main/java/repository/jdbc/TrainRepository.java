package repository.jdbc;

import domain.Train;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import repository.interfaces.ITrainRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class TrainRepository implements ITrainRepository {
    private static final Logger logger = LogManager.getLogger(TrainRepository.class);
    private final DataSource dataSource;

    @Autowired
    public TrainRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public Train save(Train entity) {
        String sql = "INSERT INTO Trains (name, total_capacity) VALUES (?, ?)";
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setString(1, entity.getName());
            preStmt.setInt(2, entity.getTotalCapacity());
            preStmt.executeUpdate();
            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: save Train", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public Optional<Train> findOne(Long id) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Trains WHERE id=?")) {
            preStmt.setLong(1, id);
            try (ResultSet rs = preStmt.executeQuery()) {
                if (rs.next()) return Optional.of(new Train(id, rs.getString("name"), rs.getInt("total_capacity")));
            }
        } catch (SQLException ex) {
            logger.error("DB Error: findOne Train", ex);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Train> findAll() {
        ArrayList<Train> list = new ArrayList<>();
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Trains");
             ResultSet rs = preStmt.executeQuery()) {
            while (rs.next()) list.add(new Train(rs.getLong("id"), rs.getString("name"), rs.getInt("total_capacity")));
        } catch (SQLException ex) {
            logger.error("DB Error: findAll Trains", ex);
        }
        return list;
    }

    @Override
    public Train update(Train entity) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("UPDATE Trains SET name=?, total_capacity=? WHERE id=?")) {
            preStmt.setString(1, entity.getName());
            preStmt.setInt(2, entity.getTotalCapacity());
            preStmt.setLong(3, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: update Train", ex);
            throw new RuntimeException(ex);
        }
        return entity;
    }

    @Override
    public void delete(Long id) {
        Connection con = getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Trains WHERE id=?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("DB Error: delete Train", ex);
        }
    }
}