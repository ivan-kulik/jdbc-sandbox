package org.course.jdbcsandbox.jdbc;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class JdbcExecutor {

    private final DataSource dataSource;
    private final JdbcExceptionTranslator exceptionTranslator;

    public JdbcExecutor(
            DataSource dataSource,
            JdbcExceptionTranslator exceptionTranslator
    ) {
        this.dataSource = dataSource;
        this.exceptionTranslator = exceptionTranslator;
    }

    public <T> T query(
            String sqlQuery,
            PreparedStatementSetter statementSetter,
            ResultSetExtractor<T> resultSetExtractor
    ) {
        try (Connection connection = this.dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlQuery)
        ) {
            statementSetter.setValues(statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSetExtractor.extract(resultSet);
            }

        } catch (SQLException e) {
            throw this.exceptionTranslator.translate(
                    "Executing query", sqlQuery, e
            );
        }
    }

    public int update(
            String sqlQuery,
            PreparedStatementSetter statementSetter
    ) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)
        ) {
            statementSetter.setValues(statement);

            return statement.executeUpdate();

        } catch (SQLException e) {
            throw this.exceptionTranslator.translate(
                    "Executing update", sqlQuery, e
            );
        }
    }
}
