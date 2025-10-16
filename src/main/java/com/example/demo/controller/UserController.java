package com.example.demo.controller;

import com.example.demo.model.UserProfile;
import com.example.demo.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/{userId}/profile/sequential")
    public ResponseEntity<UserProfile> getUserProfileSequential(@PathVariable String userId)
            throws Exception {
        long startTime = System.currentTimeMillis();
        UserProfile profile = userProfileService.getUserProfileSequential(userId);
        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok()
                .header("X-Execution-Time", duration + "ms")
                .body(profile);
    }

    @GetMapping("/{userId}/profile/async")
    public CompletableFuture<ResponseEntity<UserProfile>> getUserProfileAsync(
            @PathVariable String userId) {
        long startTime = System.currentTimeMillis();

        return userProfileService.getUserProfileAsync(userId)
                .thenApply(profile -> {
                    long duration = System.currentTimeMillis() - startTime;
                    return ResponseEntity.ok()
                            .header("X-Execution-Time", duration + "ms")
                            .body(profile);
                });
    }

    @GetMapping("/{userId}/profile/structured")
    public ResponseEntity<UserProfile> getUserProfileStructured(@PathVariable String userId) {
        long startTime = System.currentTimeMillis();
        UserProfile profile = userProfileService.getUserProfileStructured(userId);
        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok()
                .header("X-Execution-Time", duration + "ms")
                .body(profile);
    }

    @GetMapping("/{userId}/profile/structured-timeout")
    public ResponseEntity<UserProfile> getUserProfileWithTimeout(@PathVariable String userId) {
        long startTime = System.currentTimeMillis();
        try {
            UserProfile profile = userProfileService.getUserProfileWithTimeout(
                    userId, Duration.ofSeconds(2)
            );
            long duration = System.currentTimeMillis() - startTime;

            return ResponseEntity.ok()
                    .header("X-Execution-Time", duration + "ms")
                    .body(profile);
        } catch (RuntimeException e) {
            long duration = System.currentTimeMillis() - startTime;
            return ResponseEntity.badRequest()
                    .header("X-Execution-Time", duration + "ms")
                    .body(null);
        }
    }

    @GetMapping("/{userId}/profile/complex")
    public ResponseEntity<UserProfile> getUserProfileComplex(@PathVariable String userId) {
        long startTime = System.currentTimeMillis();
        UserProfile profile = userProfileService.getUserProfileComplex(userId);
        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok()
                .header("X-Execution-Time", duration + "ms")
                .body(profile);
    }
}
