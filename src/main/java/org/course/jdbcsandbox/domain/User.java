package org.course.jdbcsandbox.domain;

import java.time.Instant;

public record User(
        Long id,
        String username,
        String email,
        Instant createdAt
) {
}
