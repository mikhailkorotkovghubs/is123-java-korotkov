package com.familybudget.service;
import com.familybudget.model.Transaction;
import java.util.List;
public interface TransactionService {
    List<Transaction> getTransactionsForCurrentUser();
    Transaction saveTransaction(Transaction transaction);
    List<Transaction> getAllTransactions();
}