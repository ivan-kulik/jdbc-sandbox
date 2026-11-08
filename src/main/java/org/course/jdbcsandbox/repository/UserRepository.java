package org.course.jdbcsandbox.repository;

import org.course.jdbcsandbox.domain.User;
import org.course.jdbcsandbox.dto.UpdateUserRequest;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    User save(String username, String email);

    Optional<User> update(Long id, UpdateUserRequest request);

    boolean deleteById(Long id);

    boolean existsById(Long id);

    List<User> findByUsernamePrefix(String prefix, int limit);
}
