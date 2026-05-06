package com.familybudget;

import com.familybudget.model.*;
import com.familybudget.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootApplication
@EnableCaching
public class FamilyBudgetApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamilyBudgetApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    private Income createIncome(BigDecimal amount, String source, User user, String description, LocalDateTime date) {
        Income income = new Income(amount, source, user, description);
        income.setDate(date);
        return income;
    }

    private void createTransaction(TransactionRepository repo, Category cat, User user, BigDecimal amount, String desc, LocalDateTime date) {
        Transaction t = new Transaction(amount, cat, user, desc);
        t.setDate(date);
        repo.save(t);
    }
}