package com.familybudget.observer;
import com.familybudget.model.User;
import com.familybudget.observer.BudgetListener;
import com.familybudget.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class LogNotificationListener implements BudgetListener {
    private static final Logger log = LoggerFactory.getLogger(LogNotificationListener.class);
    public LogNotificationListener(NotificationService notificationService) { notificationService.registerListener(this); }
    @Override
    public void onBudgetExceeded(User user, BigDecimal limit, BigDecimal currentSpent) {
        log.warn(" BUDGET ALERT: Пользователь {} превысил лимит! Лимит: {}, Потрачено: {}", user.getUsername(), limit, currentSpent);
    }
}