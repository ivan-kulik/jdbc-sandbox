package org.course.jdbcsandbox.repository;

import org.course.jdbcsandbox.domain.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    User save(String username, String email);

    void deleteById(Long id);
}
