package com.familybudget.service;
import com.familybudget.model.Transaction;
import com.familybudget.model.User;
import com.familybudget.repository.TransactionRepository;
import com.familybudget.repository.UserRepository;
import com.familybudget.service.NotificationService;
import com.familybudget.service.TransactionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TransactionServiceImpl(TransactionRepository transactionRepository, UserRepository userRepository, NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) throw new RuntimeException("Не авторизован");
        String username = auth.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsForCurrentUser() {
        User user = getCurrentUser();
        if (user.getRole().toString().equals("ADMIN") || user.getRole().toString().equals("OWNER")) {
            if (user.getFamily() != null) return transactionRepository.findByUserFamilyIdOrderByDateDesc(user.getFamily().getId());
        }
        return transactionRepository.findByUserIdOrderByDateDesc(user.getId());
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        User currentUser = getCurrentUser();
        transaction.setUser(currentUser);
        Transaction saved = transactionRepository.save(transaction);
        BigDecimal currentTotal = getTransactionsForCurrentUser().stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        notificationService.checkBudget(currentUser, new BigDecimal("100000"), currentTotal);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() { return transactionRepository.findAll(); }
}