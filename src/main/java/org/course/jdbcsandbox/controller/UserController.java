package org.course.jdbcsandbox.controller;

import jakarta.validation.Valid;
import org.course.jdbcsandbox.domain.User;
import org.course.jdbcsandbox.dto.CreateUserRequest;
import org.course.jdbcsandbox.dto.UpdateUserRequest;
import org.course.jdbcsandbox.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<User> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        User user = userService.create(
                request.username(),
                request.email()
        );

        URI location = URI.create("/api/v1/users/" + user.id());

        return ResponseEntity.created(location).body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(
            @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateById(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.update(
                id,
                request.username(),
                request.email()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable(name = "id") Long id
    ) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
