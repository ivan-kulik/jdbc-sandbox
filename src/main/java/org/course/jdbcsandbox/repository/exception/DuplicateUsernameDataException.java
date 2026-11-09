package org.course.jdbcsandbox.repository.exception;

public class DuplicateUsernameDataException extends RuntimeException {

    public DuplicateUsernameDataException(Throwable cause) {
        super("Username already exists in database", cause);
    }
}
