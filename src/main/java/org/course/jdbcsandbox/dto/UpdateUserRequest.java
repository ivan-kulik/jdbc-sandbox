package org.course.jdbcsandbox.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 3, max = 60) String username,
        @Size(max = 60) @Email String email
) {
}
