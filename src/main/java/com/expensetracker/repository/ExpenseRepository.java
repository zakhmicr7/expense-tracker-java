package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ExpenseRepository {

    private final JdbcTemplate jdbc;

    public ExpenseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Expense> rowMapper = (rs, rowNum) -> new Expense(
        rs.getLong("id"),
        rs.getDouble("amount"),
        rs.getString("category"),
        rs.getString("description"),
        rs.getString("date")
    );

    public List<Expense> findAll() {
        return jdbc.query("SELECT * FROM expenses ORDER BY date DESC, id DESC", rowMapper);
    }

    public Expense save(Expense expense) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO expenses (amount, category, description, date) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setDouble(1, expense.getAmount());
            ps.setString(2, expense.getCategory());
            ps.setString(3, expense.getDescription());
            ps.setString(4, expense.getDate());
            return ps;
        }, keyHolder);
        expense.setId(keyHolder.getKey().longValue());
        return expense;
    }

    public boolean deleteById(Long id) {
        return jdbc.update("DELETE FROM expenses WHERE id = ?", id) > 0;
    }
}
