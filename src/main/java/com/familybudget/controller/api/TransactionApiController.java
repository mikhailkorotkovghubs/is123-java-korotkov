package com.familybudget.controller.api;
import com.familybudget.model.Transaction;
import com.familybudget.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionApiController {
    private final TransactionService transactionService;
    public TransactionApiController(TransactionService transactionService) { this.transactionService = transactionService; }
    @GetMapping public ResponseEntity<List<Transaction>> getAll() { return ResponseEntity.ok(transactionService.getTransactionsForCurrentUser()); }
    @PostMapping public ResponseEntity<Transaction> create(@RequestBody Transaction transaction) {
        try { return ResponseEntity.ok(transactionService.saveTransaction(transaction)); } catch (Exception e) { return ResponseEntity.badRequest().build(); }
    }
}