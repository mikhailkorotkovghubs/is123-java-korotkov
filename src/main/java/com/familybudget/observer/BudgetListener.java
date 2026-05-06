package com.familybudget.observer;
import com.familybudget.model.User;
import java.math.BigDecimal;
public interface BudgetListener { void onBudgetExceeded(User user, BigDecimal limit, BigDecimal currentSpent); }