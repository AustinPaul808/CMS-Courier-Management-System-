package com.courier.management;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Database CRUD operations for the couriers table using Spring JdbcTemplate.
 *
 * Table schema (already exists in Supabase):
 *   id INTEGER PRIMARY KEY,
 *   sender VARCHAR(100), receiver VARCHAR(100), address TEXT,
 *   weight DOUBLE PRECISION, courier_type VARCHAR(20), price NUMERIC(10,2)
 */
@Repository
public class CourierRepository {

    private final JdbcTemplate jdbcTemplate;

    public CourierRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ------------------------------------------------------------------
    // INSERT
    // ------------------------------------------------------------------

    /**
     * Persist a new courier record into the database.
     * Uses a parameterized query — no string concatenation.
     */
    public void save(Courier courier) {
        String sql = "INSERT INTO couriers (id, sender, receiver, address, weight, courier_type, price) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                courier.getId(),
                courier.getSender(),
                courier.getReceiver(),
                courier.getAddress(),
                courier.getWeight(),
                courier.getType(),
                courier.getPrice()
        );
    }

    // ------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------

    /**
     * Load all courier records from the database.
     * Maps each row to the correct subclass (DomesticCourier or InternationalCourier).
     */
    public List<Courier> findAll() {
        String sql = "SELECT id, sender, receiver, address, weight, courier_type, price FROM couriers ORDER BY id";
        return jdbcTemplate.query(sql, new CourierRowMapper());
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    /**
     * Delete a courier record by its primary key.
     * Returns the number of rows affected (1 if deleted, 0 if not found).
     */
    public int deleteById(int id) {
        String sql = "DELETE FROM couriers WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // ------------------------------------------------------------------
    // Row Mapper
    // ------------------------------------------------------------------

    /**
     * Maps a database row to the correct Courier subclass.
     * The courier_type column determines whether to create a
     * DomesticCourier or InternationalCourier.
     */
    private static class CourierRowMapper implements RowMapper<Courier> {
        @Override
        public Courier mapRow(ResultSet rs, int rowNum) throws SQLException {
            int    id          = rs.getInt("id");
            String sender      = rs.getString("sender");
            String receiver    = rs.getString("receiver");
            String address     = rs.getString("address");
            double weight      = rs.getDouble("weight");
            String courierType = rs.getString("courier_type");

            // Reconstruct the correct subclass; price is recalculated by the constructor
            // which is consistent with the application's pricing logic.
            if ("Domestic".equalsIgnoreCase(courierType)) {
                return new DomesticCourier(id, sender, receiver, address, weight);
            } else {
                return new InternationalCourier(id, sender, receiver, address, weight);
            }
        }
    }
}

