package org.course.jdbcsandbox.transactions.propagation;

import org.course.jdbcsandbox.jdbc.JdbcExecutor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PropagationDemoRepository {

    private final JdbcExecutor jdbcExecutor;

    public PropagationDemoRepository(JdbcExecutor jdbcExecutor) {
        this.jdbcExecutor = jdbcExecutor;
    }

    public void save(Long id, String description) {
        String sqlQuery = """
                INSERT INTO tx_propagation_demo (id, description)
                VALUES (?, ?);
                """;
        int affectedRows = jdbcExecutor.update(
                sqlQuery,
                statement -> {
                    statement.setLong(1, id);
                    statement.setString(2, description);
                }
        );
        if (affectedRows != 1) {
            throw new IllegalStateException("SQL insert went wrong");
        }
    }

    public List<Long> findAllIds() {
        String sqlQuery = """
                SELECT id
                FROM tx_propagation_demo
                ORDER BY id;
                """;
        return jdbcExecutor.query(
                sqlQuery,
                statement -> {},
                resultSet -> {
                    List<Long> allIds = new ArrayList<>();
                    while (resultSet.next()) {
                        allIds.add(resultSet.getLong(1));
                    }
                    return allIds;
                }
        );
    }
}
