package com.familybudget.controller.report;

import com.familybudget.model.*;
import com.familybudget.repository.*;
import com.familybudget.service.TransactionService;
import com.familybudget.util.ExcelExporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
public class ReportExportController {

    private final TransactionService transactionService;
    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
    private final SavingsGoalRepository goalRepository;
    private final AuditLogRepository auditLogRepository;

    public ReportExportController(TransactionService transactionService, IncomeRepository incomeRepository,
                                  UserRepository userRepository, SavingsGoalRepository goalRepository,
                                  AuditLogRepository auditLogRepository) {
        this.transactionService = transactionService;
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping(value = "/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportToExcel(Authentication authentication) throws IOException {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Transaction> transactions = transactionService.getTransactionsForCurrentUser();

        List<Income> allIncomes = incomeRepository.findAll();
        List<Income> familyIncomes = allIncomes.stream()
                .filter(income -> income.getUser().getFamily().getId().equals(user.getFamily().getId()))
                .collect(Collectors.toList());

        byte[] excelBytes = ExcelExporter.exportFullReport(transactions, familyIncomes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String fileName = "family_budget_report_" + LocalDateTime.now().toLocalDate() + ".xlsx";
        headers.setContentDispositionFormData("attachment", fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    @GetMapping("/backup")
    public ResponseEntity<byte[]> downloadBackup(Authentication authentication) throws IOException {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Long familyId = user.getFamily().getId();

        Map<String, Object> backupData = new HashMap<>();
        Family realFamily = user.getFamily();

        backupData.put("exportDate", LocalDateTime.now().toString());
        backupData.put("family", realFamily);
        backupData.put("users", userRepository.findAllByFamilyId(familyId));
        backupData.put("transactions", transactionService.getTransactionsForCurrentUser());

        backupData.put("incomes", incomeRepository.findAll().stream()
                .filter(i -> i.getUser().getFamily().getId().equals(familyId))
                .collect(Collectors.toList()));

        backupData.put("goals", goalRepository.findByFamilyId(familyId));

        // Последние 50 записей аудита
        backupData.put("auditLogs", auditLogRepository.findTop20ByOrderByTimestampDesc());

        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        mapper.findAndRegisterModules();

        byte[] jsonBytes = mapper.writeValueAsBytes(backupData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String fileName = "family_budget_backup_" + LocalDateTime.now().toLocalDate() + ".json";
        headers.setContentDispositionFormData("attachment", fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(jsonBytes);
    }
}