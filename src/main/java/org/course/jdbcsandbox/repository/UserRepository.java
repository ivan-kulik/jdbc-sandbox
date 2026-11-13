package org.course.jdbcsandbox.repository;

import org.course.jdbcsandbox.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(String username, String email);

    boolean existsById(Long id);

    Optional<User> findById(Long id);

    List<User> findByUsernamePrefix(String prefix, int limit);

    Optional<User> update(Long id, String username, String email);

    boolean deleteById(Long id);
}
