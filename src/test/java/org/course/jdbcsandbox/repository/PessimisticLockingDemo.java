package org.course.jdbcsandbox.repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PessimisticLockingDemo {

    private final DataSource dataSource;

    public PessimisticLockingDemo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private enum Outcome {
        REMOVED,
        KEPT_ON_CALL
    }

    public record Result(
            int removedCount,
            int keptOnCallCount,
            int onCallCount
    ) {
    }

    private void dropTestTable() throws SQLException {
        String sqlQuery = """
                DROP TABLE IF EXISTS doctors_for_update_demo;
                """;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(sqlQuery);
        }
    }

    private void prepareTestTable() throws SQLException {
        String createTableSqlQuery = """
                CREATE TABLE doctors_for_update_demo (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    on_call BOOLEAN NOT NULL
                );
                """;
        String insertValuesSqlQuery = """
                INSERT INTO doctors_for_update_demo (id, name, on_call)
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
                SELECT COUNT(*) FROM doctors_for_update_demo
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
            CountDownLatch bothReadyLatch,
            CountDownLatch startLatch
    ) throws SQLException, RuntimeException, InterruptedException {
        String selectSqlQuery = """
                SELECT id, on_call FROM doctors_for_update_demo
                ORDER BY id FOR UPDATE;
                """;
        String updateSqlQuery = """
                UPDATE doctors_for_update_demo
                SET on_call = FALSE
                WHERE id = ? AND on_call = TRUE;
                """;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);

            try {
                bothReadyLatch.countDown();

                boolean ready = startLatch.await(10, TimeUnit.SECONDS);
                if (!ready) {
                    throw new IllegalStateException("Timed out waiting for updates");
                }

                int onCallCount = 0;
                try (ResultSet resultSet = statement.executeQuery(selectSqlQuery)) {
                    while (resultSet.next()) {
                        if (resultSet.getBoolean("on_call")) {
                            onCallCount += 1;
                        }
                    }
                }

                if (onCallCount <= 1) {
                    connection.commit();
                    return Outcome.KEPT_ON_CALL;
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(updateSqlQuery)) {
                    preparedStatement.setLong(1, doctorId);

                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows != 1) {
                        throw new IllegalStateException("SQL query UPDATE went wrong.");
                    }

                    connection.commit();
                    return Outcome.REMOVED;
                }
            } catch (SQLException | RuntimeException | InterruptedException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }

                throw exception;
            }
        }
    }

    public Result demonstratePessimisticLocking() throws Exception {
        dropTestTable();

        try {
            prepareTestTable();

            int removedCount = 0;
            int keptOnCallCount = 0;

            try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
                CountDownLatch bothReadyLatch = new CountDownLatch(2);
                CountDownLatch startLatch = new CountDownLatch(1);

                Future<Outcome> firstFuture = executorService.submit(
                        () -> executeTransaction(1, bothReadyLatch, startLatch)
                );
                Future<Outcome> secondFuture = executorService.submit(
                        () -> executeTransaction(2, bothReadyLatch, startLatch)
                );

                boolean ready = bothReadyLatch.await(10, TimeUnit.SECONDS);
                if (!ready) {
                    throw new IllegalStateException("Transactions did not finish reading");
                }
                startLatch.countDown();

                PessimisticLockingDemo.Outcome firstOutcome = firstFuture.get(10, TimeUnit.SECONDS);
                PessimisticLockingDemo.Outcome secondOutcome = secondFuture.get(10, TimeUnit.SECONDS);

                if (firstOutcome.equals(Outcome.REMOVED)) {
                    removedCount += 1;
                } else {
                    keptOnCallCount += 1;
                }
                if (secondOutcome.equals(Outcome.REMOVED)) {
                    removedCount += 1;
                } else {
                    keptOnCallCount += 1;
                }
            }

            int onCallCount;
            try (Connection connection = dataSource.getConnection()) {
                onCallCount = countOnCall(connection);
            }

            return new Result(
                    removedCount, keptOnCallCount, onCallCount
            );
        } finally {
            dropTestTable();
        }
    }
}
