package org.course.jdbcsandbox.repository;

import org.course.jdbcsandbox.domain.User;
import org.course.jdbcsandbox.jdbc.JdbcExecutor;
import org.course.jdbcsandbox.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final UserMapper userMapper;
    private final JdbcExecutor jdbcExecutor;

    public JdbcUserRepository(
            UserMapper userMapper,
            JdbcExecutor jdbcExecutor
    ) {
        this.userMapper = userMapper;
        this.jdbcExecutor = jdbcExecutor;
    }

    @Override
    public User save(String username, String email) {
        String sqlQuery = """
                INSERT INTO users (username, email)
                VALUES (?, ?)
                RETURNING id, username, email, created_at;
                """;
        return jdbcExecutor.query(
                sqlQuery,
                statement -> {
                    statement.setString(1, username);
                    statement.setString(2, email);
                },
                resultSet -> {
                    if (!resultSet.next()) {
                        throw new IllegalStateException("INSERT returned no user");
                    }
                    return userMapper.map(resultSet);
                }
        );
    }

    @Override
    public boolean existsById(Long id) {
        String sqlQuery = """
                SELECT EXISTS (SELECT 1 FROM users WHERE id = ?);
                """;
        return jdbcExecutor.query(
                sqlQuery,
                statement -> statement.setLong(1, id),
                resultSet -> {
                    if (!resultSet.next()) {
                        throw new IllegalStateException("EXISTS query returned no result");
                    }
                    return resultSet.getBoolean(1);
                }
        );
    }

    @Override
    public Optional<User> findById(Long id) {
        String sqlQuery = """
                SELECT id, username, email, created_at
                FROM users
                WHERE id = ?;
                """;
        return jdbcExecutor.query(
                sqlQuery,
                statement -> statement.setLong(1, id),
                resultSet -> {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(this.userMapper.map(resultSet));
                }
        );
    }

    @Override
    public List<User> findByUsernamePrefix(String prefix, int limit) {
        String sqlQuery = """
                SELECT id, username, email, created_at
                FROM users
                WHERE username LIKE ?
                ORDER BY username LIMIT ?;
                """;
        return jdbcExecutor.query(
                sqlQuery,
                statement -> {
                    statement.setString(1, prefix + "%");
                    statement.setInt(2, limit);
                },
                resultSet -> {
                    List<User> users = new ArrayList<>();
                    while (resultSet.next()) {
                        users.add(this.userMapper.map(resultSet));
                    }
                    return users;
                }
        );
    }

    @Override
    public Optional<User> update(Long id, String username, String email) {
        String sqlQuery = """
                UPDATE users SET username = ?, email = ?
                WHERE id = ?
                RETURNING id, username, email, created_at
                """;
        return jdbcExecutor.query(
                sqlQuery,
                statement -> {
                    statement.setString(1, username);
                    statement.setString(2, email);
                    statement.setLong(3, id);
                },
                resultSet -> {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(this.userMapper.map(resultSet));
                }
        );
    }

    @Override
    public boolean deleteById(Long id) {
        String sqlQuery = """
                DELETE FROM users WHERE id = ?;
                """;
        int affectedRows = jdbcExecutor.update(
                sqlQuery,
                statement -> statement.setLong(1, id)
        );
        return affectedRows > 0;
    }
}
