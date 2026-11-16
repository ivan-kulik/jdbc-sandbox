package org.course.jdbcsandbox.repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTransferDemo {

    private final DataSource dataSource;

    public JdbcTransferDemo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void transfer(
            long fromAccountId,
            long toAccountId,
            BigDecimal amount
    ) throws SQLException, IllegalArgumentException {
        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException("Account's ids must be different");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        String sqlQuery1 = """
                UPDATE accounts
                SET balance = balance - ?
                WHERE id = ? AND balance >= ?;
                """;

        String sqlQuery2 = """
                UPDATE accounts
                SET balance = balance + ?
                WHERE id = ?;
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement1 = connection.prepareStatement(sqlQuery1);
             PreparedStatement statement2 = connection.prepareStatement(sqlQuery2)
        ) {
            connection.setAutoCommit(false);

            statement1.setBigDecimal(1, amount);
            statement1.setLong(2, fromAccountId);
            statement1.setBigDecimal(3, amount);

            statement2.setBigDecimal(1, amount);
            statement2.setLong(2, toAccountId);

            try {
                int affectedRows1 = statement1.executeUpdate();
                if (affectedRows1 != 1) {
                    throw new IllegalStateException(
                            "Source account not found or insufficient funds"
                    );
                }

                int affectedRows2 = statement2.executeUpdate();
                if (affectedRows2 != 1) {
                    throw new IllegalStateException(
                            "Destination account not found"
                    );
                }

                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }
        }
    }
}
