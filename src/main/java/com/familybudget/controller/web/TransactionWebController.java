package com.familybudget.controller.web;

import com.familybudget.model.Category;
import com.familybudget.model.Transaction;
import com.familybudget.model.User;
import com.familybudget.repository.CategoryRepository;
import com.familybudget.repository.UserRepository;
import com.familybudget.service.TransactionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
public class TransactionWebController {

    private final TransactionService transactionService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionWebController(TransactionService transactionService, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/add")
    public RedirectView addTransaction(String amount, Long categoryId, String description, Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) throw new RuntimeException("User not found");

            Optional<Category> catOpt = categoryRepository.findById(categoryId);
            if (catOpt.isEmpty()) throw new RuntimeException("Category not found");

            Transaction t = new Transaction();
            t.setAmount(new BigDecimal(amount));
            t.setCategory(catOpt.get());
            t.setUser(userOpt.get());
            t.setDescription(description);

            transactionService.saveTransaction(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RedirectView("/dashboard");
    }
}