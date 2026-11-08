package org.course.jdbcsandbox.jdbc;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Component
public class JdbcExceptionTranslator {

    private final SQLExceptionTranslator translator;

    public JdbcExceptionTranslator(DataSource dataSource) {
        this.translator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    public DataAccessException translate(
            String taskDescription,
            String sql,
            SQLException exception
    ) {
        DataAccessException translatedException = this.translator.translate(
                taskDescription, sql, exception);

        if (translatedException != null) {
            return translatedException;
        }

        return new UncategorizedSQLException(taskDescription, sql, exception);
    }
}
