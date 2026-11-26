package org.course.jdbcsandbox.transactions.propagation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PropagationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PropagationOuterService outerService;

    @Autowired
    private PropagationDemoRepository repository;

    @BeforeEach
    void prepareTestTable() throws SQLException {
        String sqlQuery = """
                CREATE TABLE tx_propagation_demo (
                    id BIGINT PRIMARY KEY,
                    description VARCHAR(100) NOT NULL
                );
                """;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(sqlQuery);
        }
    }

    @AfterEach
    void dropTestTable() throws SQLException {
        String sqlQuery = """
                DROP TABLE IF EXISTS tx_propagation_demo;
                """;
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(sqlQuery);
        }
    }

    @Test
    void requiredRollsBackEverything() {
        assertThrows(
                UnexpectedRollbackException.class,
                () -> outerService.requiredScenario()
        );

        assertEquals(List.of(), repository.findAllIds());
    }

    @Test
    void requiresNewCommitsIndependently() {
        assertThrows(
                IllegalStateException.class,
                () -> outerService.requiresNewScenario()
        );

        assertEquals(List.of(2L), repository.findAllIds());
    }

    @Test
    void nestedRollsBackToSavepoint() {
        outerService.nestedScenario();

        assertEquals(
                List.of(1L, 3L),
                repository.findAllIds()
        );
    }
}
