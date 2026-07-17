package com.expensetracker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
@Slf4j
public class DatabaseConnectionTest implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("     DATABASE CONNECTION TEST");
        log.info("========================================");

        try (Connection connection = dataSource.getConnection()) {
            log.info("✅ Database connection successful!");
            log.info("✅ Database: {}", connection.getCatalog());
            log.info("✅ URL: {}", connection.getMetaData().getURL());
            log.info("✅ Username: {}", connection.getMetaData().getUserName());
            log.info("✅ Driver: {}", connection.getMetaData().getDriverName());

            // Test if tables exist
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rs.next()) {
                    log.info("✅ users table exists with {} records", rs.getInt(1));
                }

                rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
                if (rs.next()) {
                    log.info("✅ categories table exists with {} records", rs.getInt(1));
                }

                rs = stmt.executeQuery("SELECT COUNT(*) FROM expenses");
                if (rs.next()) {
                    log.info("✅expenses table exists with {} records", rs.getInt(1));
                }

                rs = stmt.executeQuery("SELECT COUNT(*) FROM budgets");
                if (rs.next()) {
                    log.info("✅ budgets table exists with {} records", rs.getInt(1));
                }
            }

            log.info("========================================");
            log.info("✅ All database checks passed!");
            log.info("========================================");

        } catch (Exception e) {
            log.error("❌ Database connection failed!", e);
        }
    }
}