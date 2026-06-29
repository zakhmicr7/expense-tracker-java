package com.expensetracker.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigration {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigration.class);

    private final JdbcTemplate jdbc;

    public DatabaseMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void migrate() {
        try {
            jdbc.execute(
                "ALTER TABLE expenses ADD COLUMN user_id INTEGER NOT NULL DEFAULT 0 REFERENCES users(id)"
            );
            log.info("DatabaseMigration: added user_id column to expenses table");
        } catch (Exception e) {
            log.debug("DatabaseMigration: user_id column already exists, skipping ALTER TABLE");
        }
    }
}
