package com.familybudget.service;
import com.familybudget.model.User;
import com.familybudget.observer.BudgetListener;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private final List<BudgetListener> listeners = new ArrayList<>();
    public void registerListener(BudgetListener listener) { listeners.add(listener); }
    public void checkBudget(User user, BigDecimal limit, BigDecimal spent) {
        if (spent.compareTo(limit) > 0) {
            for (BudgetListener listener : listeners) listener.onBudgetExceeded(user, limit, spent);
        }
    }
}