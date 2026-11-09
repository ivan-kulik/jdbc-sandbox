package org.course.jdbcsandbox.repository.exception;

public class DuplicateEmailDataException extends RuntimeException {

    public DuplicateEmailDataException(Throwable cause) {
        super("Email already exists in database", cause);
    }
}
