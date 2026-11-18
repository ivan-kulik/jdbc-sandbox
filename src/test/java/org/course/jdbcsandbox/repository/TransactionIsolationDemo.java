package org.course.jdbcsandbox.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionIsolationDemo {

    private final DataSource dataSource;

    private static final Logger logger =
            LoggerFactory.getLogger(TransactionIsolationDemo.class);

    public TransactionIsolationDemo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public BigDecimal demonstrateNonRepeatableRead(
            int isolationLevel,
            String messageInfo
    ) throws SQLException {
        setInitialBalance();

        String sqlQuery1 = """
                SELECT balance from Accounts
                WHERE id = 1;
                """;

        String sqlQuery2 = """
                UPDATE Accounts
                SET balance = 700
                WHERE id = 1;
                """;

        try (Connection connection1 = dataSource.getConnection();
             Connection connection2 = dataSource.getConnection();
             PreparedStatement statement1 = connection1.prepareStatement(sqlQuery1);
             PreparedStatement statement2 = connection2.prepareStatement(sqlQuery2)
        ) {
            connection1.setTransactionIsolation(isolationLevel);

            connection1.setAutoCommit(false);
            connection2.setAutoCommit(false);

            try {
                BigDecimal balanceBefore;
                try (ResultSet resultSet = statement1.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalStateException("SELECT returned no balance");
                    }
                    balanceBefore = resultSet.getBigDecimal(1);
                }

                int affectedRows = statement2.executeUpdate();
                if (affectedRows != 1) {
                    throw new IllegalStateException("SQL query UPDATE went wrong.");
                }
                connection2.commit();

                BigDecimal balanceAfter;
                try (ResultSet resultSet = statement1.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalStateException("SELECT returned no balance");
                    }
                    balanceAfter = resultSet.getBigDecimal(1);
                }

                logResult(messageInfo, balanceBefore, balanceAfter);

                connection1.rollback();

                return balanceAfter;
            } catch (SQLException | RuntimeException exception) {
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

    private void setInitialBalance() throws SQLException {
        String sqlQuery = """
                UPDATE Accounts
                SET balance = 1000
                WHERE id = 1;
                """;
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlQuery)
        ) {
            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new IllegalStateException("SQL query UPDATE went wrong.");
            }
        }
    }

    private void logResult(
            String messageInfo,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter
    ) {
        logger.info(messageInfo);
        logger.info("First read: {}", balanceBefore);
        logger.info("Second read: {}", balanceAfter);
    }
}
