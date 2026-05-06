package com.familybudget.controller.api;

import com.familybudget.model.Income;
import com.familybudget.repository.IncomeRepository;
import com.familybudget.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/incomes")
public class ApiIncomeController {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    public ApiIncomeController(IncomeRepository incomeRepository, UserRepository userRepository) {
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Income>> getAll(Authentication authentication) {
        String username = authentication.getName();
        var user = userRepository.findByUsername(username).orElseThrow();
        Long familyId = user.getFamily().getId();

        List<Income> familyIncomes = incomeRepository.findAll().stream()
                .filter(i -> i.getUser().getFamily().getId().equals(familyId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(familyIncomes);
    }
}