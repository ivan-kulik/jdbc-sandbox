package org.course.jdbcsandbox.service.exception;

public class UsernameAlreadyExitsException extends RuntimeException {

    public UsernameAlreadyExitsException(String username) {
        super("User with username: " + username + " already exists");
    }
}
