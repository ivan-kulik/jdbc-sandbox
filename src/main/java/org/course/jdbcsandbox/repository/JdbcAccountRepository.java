package org.course.jdbcsandbox.repository;

import org.course.jdbcsandbox.jdbc.JdbcExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class JdbcAccountRepository implements AccountRepository {

    private final JdbcExecutor jdbcExecutor;

    public JdbcAccountRepository(JdbcExecutor jdbcExecutor) {
        this.jdbcExecutor = jdbcExecutor;
    }

    public int withdraw(Long accountId, BigDecimal amount) {
        String sqlQuery = """
                UPDATE accounts
                SET balance = balance - ?
                WHERE id = ? AND balance >= ?
                """;
        return jdbcExecutor.update(
                sqlQuery,
                statement -> {
                    statement.setBigDecimal(1, amount);
                    statement.setLong(2, accountId);
                    statement.setBigDecimal(3, amount);
                });
    }

    public int deposit(Long accountId, BigDecimal amount) {
        String sqlQuery = """
                UPDATE accounts
                SET balance = balance + ?
                WHERE id = ?;
                """;
        return jdbcExecutor.update(
                sqlQuery,
                statement -> {
                    statement.setBigDecimal(1, amount);
                    statement.setLong(2, accountId);
                });
    }

    public Optional<BigDecimal> findBalanceById(Long accountId) {
        String sqlQuery = """
                SELECT balance FROM accounts
                WHERE id = ?;
                """;
        return jdbcExecutor.query(
                sqlQuery,
                statement -> statement.setLong(1, accountId),
                resultSet -> {
                    if (!resultSet.next()) {
                        throw new IllegalStateException("SELECT returned no balance");
                    }
                    return Optional.of(resultSet.getBigDecimal(1));
                }
        );
    }
}
