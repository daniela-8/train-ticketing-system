package repository.jdbc;

import domain.Train;
import repository.interfaces.ITrainRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

public class TrainRepository implements ITrainRepository {
    private JdbcUtils dbUtils;

    public TrainRepository(Properties props) {
        dbUtils = new JdbcUtils(props);
    }

    @Override
    public Train save(Train entity) {
        try (Connection con = dbUtils.getConnection();
             PreparedStatement preStmt = con.prepareStatement(
                "INSERT INTO Trains (name, total_capacity) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            preStmt.setString(1, entity.getName());
            preStmt.setInt(2, entity.getTotalCapacity());
            preStmt.executeUpdate();
            try (ResultSet keys = preStmt.getGeneratedKeys()) {
                if (keys.next()) entity.setId(keys.getLong(1));
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return entity;
    }

    @Override
    public Optional<Train> findOne(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Trains WHERE id=?")) {
            preStmt.setLong(1, id);
            try (ResultSet rs = preStmt.executeQuery()) {
                if (rs.next()) return Optional.of(new Train(id, rs.getString("name"), rs.getInt("total_capacity")));
            }
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return Optional.empty();
    }

    @Override
    public Iterable<Train> findAll() {
        Connection con = dbUtils.getConnection();
        ArrayList<Train> list = new ArrayList<>();
        try (PreparedStatement preStmt = con.prepareStatement("SELECT * FROM Trains");
             ResultSet rs = preStmt.executeQuery()) {
            while (rs.next()) list.add(new Train(rs.getLong("id"), rs.getString("name"), rs.getInt("total_capacity")));
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return list;
    }

    @Override
    public void delete(Long id) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("DELETE FROM Trains WHERE id=?")) {
            preStmt.setLong(1, id);
            preStmt.executeUpdate();
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
    }

    @Override
    public Train update(Train entity) {
        Connection con = dbUtils.getConnection();
        try (PreparedStatement preStmt = con.prepareStatement("UPDATE Trains SET name=?, total_capacity=? WHERE id=?")) {
            preStmt.setString(1, entity.getName());
            preStmt.setInt(2, entity.getTotalCapacity());
            preStmt.setLong(3, entity.getId());
            preStmt.executeUpdate();
        } catch (SQLException ex) { System.err.println("Error DB: " + ex); }
        return entity;
    }
}