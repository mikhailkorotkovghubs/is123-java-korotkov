package com.familybudget.repository;

import com.familybudget.model.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByFamilyId(Long familyId);

    List<SavingsGoal> findByOwnerId(Long userId);
}