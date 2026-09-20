package org.course.jdbcsandbox.repository;

import org.course.jdbcsandbox.domain.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sqlQuery = """
                SELECT * FROM users WHERE id = ?;
                """;

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)
        ) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapResultSetToUserDomain(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User save(String username, String email) {
        String sqlQuery = """
                INSERT INTO users (username, email) VALUES (?, ?)
                RETURNING *;
                """;

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)
        ) {
            statement.setString(1, username);
            statement.setString(2, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Insert failed, no rows affected.");
                }

                return mapResultSetToUserDomain(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean deleteById(Long id) {
        String sqlQuery = """
                DELETE FROM users WHERE id = ?;
                """;

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)
        ) {
            statement.setLong(1, id);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User mapResultSetToUserDomain(ResultSet resultSet)
            throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("email"),
                resultSet.getTimestamp("createdAt").toInstant()
        );
    }
}
