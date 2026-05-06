package com.familybudget.strategy;

import com.familybudget.model.Transaction;
import com.familybudget.strategy.ReportStrategy;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MonthlyReportStrategy implements ReportStrategy {

    @Override
    public Map<String, BigDecimal> calculate(List<Transaction> transactions) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        List<Transaction> filtered = transactions.stream()
                .filter(t -> t.getDate().isAfter(startOfMonth))
                .collect(Collectors.toList());

        Map<String, BigDecimal> result = new HashMap<>();
        for (Transaction t : filtered) {
            String catName = t.getCategory().getName();
            result.merge(catName, t.getAmount(), BigDecimal::add);
        }
        return result;
    }

    @Override
    public String getStrategyName() {
        return "Ежемесячный отчет";
    }
}