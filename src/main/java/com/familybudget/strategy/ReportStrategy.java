package com.familybudget.strategy;

import com.familybudget.model.Transaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ReportStrategy {
    Map<String, BigDecimal> calculate(List<Transaction> transactions);
    String getStrategyName();
}