package com.familybudget.repository;

import com.familybudget.model.BudgetPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BudgetPlanRepository extends JpaRepository<BudgetPlan, Long> {
    Optional<BudgetPlan> findByFamilyId(Long familyId);
}