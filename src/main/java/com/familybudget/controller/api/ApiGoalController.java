package com.familybudget.controller.api;

import com.familybudget.model.SavingsGoal;
import com.familybudget.repository.SavingsGoalRepository;
import com.familybudget.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
public class ApiGoalController {

    private final SavingsGoalRepository goalRepository;
    private final UserRepository userRepository;

    public ApiGoalController(SavingsGoalRepository goalRepository, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<SavingsGoal>> getAll(Authentication authentication) {
        String username = authentication.getName();
        var user = userRepository.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(goalRepository.findByFamilyId(user.getFamily().getId()));
    }
}