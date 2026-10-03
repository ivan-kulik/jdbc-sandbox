package org.course.jdbcsandbox.repository;

import org.course.jdbcsandbox.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    User save(String username, String email);

    boolean deleteById(Long id);

    List<User> findByUsernamePrefix(String prefix, int limit);
}
