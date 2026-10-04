package org.course.jdbcsandbox.repository;

import org.course.jdbcsandbox.domain.User;
import org.course.jdbcsandbox.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final DataSource dataSource;
    private final UserMapper userMapper;

    public JdbcUserRepository(
            DataSource dataSource,
            UserMapper userMapper
    ) {
        this.dataSource = dataSource;
        this.userMapper = userMapper;
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

                return Optional.of(this.userMapper.map(resultSet));
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

                return this.userMapper.map(resultSet);
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

    @Override
    public List<User> findByUsernamePrefix(String prefix, int limit) {
        String sqlQuery = """
                SELECT id, username, email, created_at FROM users
                WHERE username LIKE ? ORDER BY username LIMIT ?;
                """;

        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery))
        {
            statement.setString(1, prefix + "%");
            statement.setInt(2, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (resultSet.next()) {
                    users.add(this.userMapper.map(resultSet));
                }
                return users;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
