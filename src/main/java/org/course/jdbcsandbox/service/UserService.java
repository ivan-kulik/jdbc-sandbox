package org.course.jdbcsandbox.service;

import org.course.jdbcsandbox.domain.User;
import org.course.jdbcsandbox.repository.UserRepository;
import org.course.jdbcsandbox.service.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(String username, String email) {
        return userRepository.save(username, email);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User update(Long id, String username, String email) {
        return userRepository.update(id, username, email)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void deleteById(Long id) {
        if (!userRepository.deleteById(id)) {
            throw new UserNotFoundException(id);
        }
    }
}
