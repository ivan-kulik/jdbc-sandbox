package org.course.jdbcsandbox.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SelectForUpdateDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectForUpdateDemo.class);

    private final DataSource dataSource;

    public SelectForUpdateDemo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void demonstrateRowLock() throws SQLException {
        String sqlQuery1 = """
                SELECT id FROM accounts WHERE id = 1 FOR UPDATE;
                """;
        String sqlQuery2 = """
                SELECT id FROM accounts WHERE id = 1 FOR UPDATE NOWAIT;
                """;

        try (Connection connection1 = dataSource.getConnection();
             Connection connection2 = dataSource.getConnection();
             Statement statement1 = connection1.createStatement();
             Statement statement2 = connection2.createStatement()
        ) {
            connection1.setAutoCommit(false);
            connection2.setAutoCommit(false);

            try {
                try (ResultSet resultSet = statement1.executeQuery(sqlQuery1)) {
                    if (!resultSet.next()) {
                        throw new IllegalStateException("SELECT returned no id");
                    }
                }

                try (ResultSet resultSet = statement2.executeQuery(sqlQuery2)) {
                    if (!resultSet.next()) {
                        throw new IllegalStateException("SELECT returned no id");
                    }
                }

                connection1.commit();
            } catch (SQLException | RuntimeException exception) {
                if (exception instanceof SQLException) {
                    LOGGER.error("SQLException state: {}", ((SQLException) exception).getSQLState());
                }

                try {
                    connection1.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                try {
                    connection2.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }
        }
    }
}
