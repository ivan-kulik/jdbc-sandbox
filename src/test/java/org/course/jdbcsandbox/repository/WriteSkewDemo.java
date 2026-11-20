package org.course.jdbcsandbox.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WriteSkewDemo {

    private final DataSource dataSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteSkewDemo.class);

    public WriteSkewDemo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private enum Outcome {
        COMMITTED,
        SERIALIZATION_FAILURE
    }

    public record Result(
            int committedCount,
            int serializationFailureCount,
            int onCallCount
    ) {
    }

    private void dropTestTable() throws SQLException {
        String sqlQuery = """
                DROP TABLE IF EXISTS doctors_write_skew_demo;
                """;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(sqlQuery);
        }
    }

    private void prepareTestTable() throws SQLException {
        String createTableSqlQuery = """
                CREATE TABLE doctors_write_skew_demo (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    on_call BOOLEAN NOT NULL
                );
                """;
        String insertValuesSqlQuery = """
                INSERT INTO doctors_write_skew_demo (id, name, on_call)
                VALUES
                    (1, 'Ivan', true),
                    (2, 'Boris', true);
                """;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(createTableSqlQuery);

            int affectedRows = statement.executeUpdate(insertValuesSqlQuery);
            if (affectedRows != 2) {
                throw new IllegalStateException("SQL query INSERT went wrong.");
            }
        }
    }

    private int countOnCall(Connection connection) throws SQLException {
        String sqlQuery = """
                SELECT COUNT(*)
                FROM doctors_write_skew_demo
                WHERE on_call = TRUE;
                """;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sqlQuery)
        ) {
            if (!resultSet.next()) {
                throw new IllegalStateException("COUNT returned no result");
            }
            return resultSet.getInt(1);
        }
    }

    private Outcome executeTransaction(
            long doctorId,
            int isolationLevel,
            CountDownLatch bothReadLatch,
            CountDownLatch startUpdatesLatch
    ) throws SQLException, InterruptedException, IllegalStateException {

        String updateSqlQuery = """
                UPDATE doctors_write_skew_demo
                SET on_call = FALSE
                WHERE id = ? AND on_call = TRUE;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateSqlQuery)
        ) {
            connection.setTransactionIsolation(isolationLevel);
            connection.setAutoCommit(false);

            try {
                int onCallCount = countOnCall(connection);
                if (onCallCount != 2) {
                    throw new IllegalStateException("Incorrect input data");
                }
                LOGGER.info("Count on call: {}", onCallCount);

                bothReadLatch.countDown();

                boolean ready = startUpdatesLatch.await(10, TimeUnit.SECONDS);
                if (!ready) {
                    throw new IllegalStateException("Timed out waiting for updates");
                }

                preparedStatement.setLong(1, doctorId);
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows != 1) {
                    throw new IllegalStateException("SQL query UPDATE went wrong.");
                }

                connection.commit();
                return Outcome.COMMITTED;
            } catch (SQLException | RuntimeException | InterruptedException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                if (exception instanceof SQLException sqlException &&
                    "40001".equals(sqlException.getSQLState())) {
                    return Outcome.SERIALIZATION_FAILURE;
                }

                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw exception;
            }
        }
    }

    public Result demonstrateWriteSkewDemo(int isolationLevel)
            throws SQLException, InterruptedException,
            ExecutionException, TimeoutException {
        dropTestTable();

        try {
            prepareTestTable();

            CountDownLatch bothReadLatch = new CountDownLatch(2);
            CountDownLatch startUpdatesLatch = new CountDownLatch(1);

            int committedCount = 0;
            int serializationFailureCount = 0;

            try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
                Future<Outcome> firstFuture = executorService.submit(
                        () -> executeTransaction(
                                1L, isolationLevel, bothReadLatch, startUpdatesLatch
                        )
                );
                Future<Outcome> secondFuture = executorService.submit(
                        () -> executeTransaction(
                                2L, isolationLevel, bothReadLatch, startUpdatesLatch
                        )
                );

                boolean ready = bothReadLatch.await(10, TimeUnit.SECONDS);
                if (!ready) {
                    throw new IllegalStateException("Transactions did not finish reading");
                }
                startUpdatesLatch.countDown();

                Outcome firstOutcome = firstFuture.get(10, TimeUnit.SECONDS);
                Outcome secondOutcome = secondFuture.get(10, TimeUnit.SECONDS);

                if (firstOutcome.equals(Outcome.COMMITTED)) {
                    committedCount += 1;
                } else {
                    serializationFailureCount += 1;
                }
                if (secondOutcome.equals(Outcome.COMMITTED)) {
                    committedCount += 1;
                } else {
                    serializationFailureCount += 1;
                }
            }

            int onCallCount;
            try (Connection connection = dataSource.getConnection()) {
                onCallCount = countOnCall(connection);
            }

            return new Result(
                    committedCount,
                    serializationFailureCount,
                    onCallCount
            );
        } finally {
            dropTestTable();
        }
    }
}
