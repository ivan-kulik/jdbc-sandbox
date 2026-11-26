package org.course.jdbcsandbox.jdbc;

import org.springframework.jdbc.datasource.DataSourceUtils;
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

    /**
     * Executes the given SQL query, mapping the result using the provided extractor. <p>
     *
     * <u>Without <b>@Transactional</b></u>: DataSourceUtils obtains a regular connection from the pool and
     * releases it after execution. In this case, its behavior is equivalent to
     * calling dataSource.getConnection() and closing it in a finally block. <p>
     * <u>Inside <b>@Transactional</b></u>: DataSourceUtils retrieves the connection bound to the current
     * Spring transaction, rather than requesting a new one from the pool
     * (unlike calling dataSource.getConnection() directly). It will not close this connection prematurely,
     * ensuring the query participates in the ongoing transaction.
     *
     * @param <T> the type of the result returned by the extractor
     * @param sqlQuery the SQL query to execute
     * @param statementSetter a callback that sets the parameters on the prepared statement
     * @param resultSetExtractor a callback that extracts the data from the ResultSet
     * @return the result object of type T extracted from the ResultSet
     */
    public <T> T query(
            String sqlQuery,
            PreparedStatementSetter statementSetter,
            ResultSetExtractor<T> resultSetExtractor
    ) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                DataSourceUtils.applyTransactionTimeout(
                        preparedStatement, dataSource
                );

                statementSetter.setValues(preparedStatement);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSetExtractor.extract(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw exceptionTranslator.translate(
                    "Executing query", sqlQuery, exception
            );
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public int update(
            String sqlQuery,
            PreparedStatementSetter statementSetter
    ) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                DataSourceUtils.applyTransactionTimeout(
                        preparedStatement, dataSource
                );

                statementSetter.setValues(preparedStatement);

                return preparedStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw exceptionTranslator.translate(
                    "Executing update", sqlQuery, exception
            );
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
