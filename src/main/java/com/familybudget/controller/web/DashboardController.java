package com.familybudget.controller.web;

import com.familybudget.model.*;
import com.familybudget.repository.*;
import com.familybudget.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final BudgetPlanRepository budgetPlanRepository;
    private final IncomeRepository incomeRepository;
    private final SavingsGoalRepository goalRepository;
    private final AuditLogRepository auditLogRepository;
    private final CategoryRepository categoryRepository;

    public DashboardController(TransactionService transactionService, UserRepository userRepository,
                               BudgetPlanRepository budgetPlanRepository, IncomeRepository incomeRepository,
                               SavingsGoalRepository goalRepository, AuditLogRepository auditLogRepository,
                               CategoryRepository categoryRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
        this.budgetPlanRepository = budgetPlanRepository;
        this.incomeRepository = incomeRepository;
        this.goalRepository = goalRepository;
        this.auditLogRepository = auditLogRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String dashboard(Model model, Authentication authentication,
                            @RequestParam(required = false) Integer year,
                            @RequestParam(required = false) Integer month) {
        if (year == null) year = LocalDateTime.now().getYear();
        if (month == null) month = LocalDateTime.now().getMonthValue();

        prepareDashboardModel(model, authentication, year, month);

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        // семейные цели
        List<SavingsGoal> familyGoals = goalRepository.findByFamilyId(user.getFamily().getId());
        //  личные цели текущего пользователя
        List<SavingsGoal> personalGoals = goalRepository.findByOwnerId(user.getId());

        // списки для отображения
        List<SavingsGoal> allGoals = new ArrayList<>();
        allGoals.addAll(familyGoals);
        allGoals.addAll(personalGoals);

        model.addAttribute("goals", allGoals);

        List<String> months = new ArrayList<>();
        for (int y = 2025; y <= 2032; y++) {
            for (int m = 1; m <= 12; m++) {
                String label = switch (m) {
                    case 1 -> "Январь";
                    case 2 -> "Февраль";
                    case 3 -> "Март";
                    case 4 -> "Апрель";
                    case 5 -> "Май";
                    case 6 -> "Июнь";
                    case 7 -> "Июль";
                    case 8 -> "Август";
                    case 9 -> "Сентябрь";
                    case 10 -> "Октябрь";
                    case 11 -> "Ноябрь";
                    case 12 -> "Декабрь";
                    default -> "";
                };
                String val = y + "-" + String.format("%02d", m);
                months.add(val + ":" + label + " " + y);
            }
        }

        model.addAttribute("availableMonths", months);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("currentPeriodKey", year + "-" + String.format("%02d", month));
        model.addAttribute("currentUser", user); // Передаем текущего пользователя в шаблон

        return "dashboard";
    }

    @GetMapping("/transactions")
    public String allTransactions(Model model, Authentication authentication,
                                  @RequestParam(required = false) Integer year,
                                  @RequestParam(required = false) Integer month,
                                  @RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) Long userId) {
        if (year == null) year = LocalDateTime.now().getYear();
        if (month == null) month = LocalDateTime.now().getMonthValue();

        prepareDashboardModel(model, authentication, year, month);

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Transaction> allTransactions = transactionService.getTransactionsForCurrentUser();
        LocalDateTime startPeriod = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endPeriod = startPeriod.plusMonths(1);

        List<Transaction> filtered = allTransactions.stream()
                .filter(t -> !t.getDate().isBefore(startPeriod) && t.getDate().isBefore(endPeriod))
                .filter(t -> categoryId == null || t.getCategory().getId().equals(categoryId))
                .filter(t -> userId == null || t.getUser().getId().equals(userId))
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());

        model.addAttribute("allTransactions", filtered);
        model.addAttribute("currentPage", "transactions");
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("currentPeriodKey", year + "-" + String.format("%02d", month));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("familyMembers", userRepository.findAllByFamilyId(user.getFamily().getId()));

        List<String> months = new ArrayList<>();
        for (int y = 2025; y <= 2032; y++) {
            for (int m = 1; m <= 12; m++) {
                String label = switch (m) {
                    case 1 -> "Январь";
                    case 2 -> "Февраль";
                    case 3 -> "Март";
                    case 4 -> "Апрель";
                    case 5 -> "Май";
                    case 6 -> "Июнь";
                    case 7 -> "Июль";
                    case 8 -> "Август";
                    case 9 -> "Сентябрь";
                    case 10 -> "Октябрь";
                    case 11 -> "Ноябрь";
                    case 12 -> "Декабрь";
                    default -> "";
                };
                String val = y + "-" + String.format("%02d", m);
                months.add(val + ":" + label + " " + y);
            }
        }
        model.addAttribute("availableMonths", months);

        return "transactions";
    }

    @GetMapping("/incomes")
    public String allIncomes(Model model, Authentication authentication,
                             @RequestParam(required = false) Integer year,
                             @RequestParam(required = false) Integer month) {
        if (year == null) year = LocalDateTime.now().getYear();
        if (month == null) month = LocalDateTime.now().getMonthValue();

        prepareDashboardModel(model, authentication, year, month);

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        List<SavingsGoal> familyGoals = goalRepository.findByFamilyId(user.getFamily().getId());
        List<SavingsGoal> personalGoals = goalRepository.findByOwnerId(user.getId());

        List<SavingsGoal> allGoals = new ArrayList<>();
        allGoals.addAll(familyGoals);
        allGoals.addAll(personalGoals);

        model.addAttribute("goals", allGoals);
        model.addAttribute("currentUser", user); // Нужно для определения прав на удаление/редактирование
        model.addAttribute("familyMembers", userRepository.findAllByFamilyId(user.getFamily().getId()));

        LocalDateTime startPeriod = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endPeriod = startPeriod.plusMonths(1);

        List<Income> allIncomes = incomeRepository.findAll();
        List<Income> filtered = allIncomes.stream()
                .filter(i -> i.getUser().getFamily().getId().equals(user.getFamily().getId()))
                .filter(i -> !i.getDate().isBefore(startPeriod) && i.getDate().isBefore(endPeriod))
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());

        model.addAttribute("incomes", filtered);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("currentPeriodKey", year + "-" + String.format("%02d", month));

        List<String> months = new ArrayList<>();
        for (int y = 2025; y <= 2032; y++) {
            for (int m = 1; m <= 12; m++) {
                String label = switch (m) {
                    case 1 -> "Январь";
                    case 2 -> "Февраль";
                    case 3 -> "Март";
                    case 4 -> "Апрель";
                    case 5 -> "Май";
                    case 6 -> "Июнь";
                    case 7 -> "Июль";
                    case 8 -> "Август";
                    case 9 -> "Сентябрь";
                    case 10 -> "Октябрь";
                    case 11 -> "Ноябрь";
                    case 12 -> "Декабрь";
                    default -> "";
                };
                String val = y + "-" + String.format("%02d", m);
                months.add(val + ":" + label + " " + y);
            }
        }
        model.addAttribute("availableMonths", months);

        return "incomes";
    }

    @GetMapping("/income/{id}")
    public String incomeDetail(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Income> incomeOpt = incomeRepository.findById(id);
        if (incomeOpt.isPresent()) {
            Income income = incomeOpt.get();
            model.addAttribute("income", income);
            model.addAttribute("dateFormatted", income.getDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")));
            model.addAttribute("userName", authentication.getName());
            return "income-detail";
        }
        return "redirect:/dashboard/incomes";
    }

    @GetMapping("/income/add")
    public String addIncomeForm(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        model.addAttribute("familyMembers", userRepository.findAllByFamilyId(user.getFamily().getId()));
        model.addAttribute("income", new Income());
        return "income-add";
    }

    @PostMapping("/income/add")
    public RedirectView addIncome(@RequestParam BigDecimal amount,
                                  @RequestParam String source,
                                  @RequestParam String description,
                                  @RequestParam Long userId,
                                  @RequestParam String date,
                                  Authentication authentication) {
        try {
            User incomeUser = userRepository.findById(userId).orElseThrow();
            Income income = new Income(amount, source, incomeUser, description);
            LocalDateTime incomeDate = LocalDateTime.parse(date.replace(" ", "T"));
            income.setDate(incomeDate);
            incomeRepository.save(income);

            auditLogRepository.save(new AuditLog(
                    authentication.getName(),
                    "ADD_INCOME",
                    "Добавлен доход: " + amount + " ₽ от " + source
            ));

            return new RedirectView("/dashboard/incomes?success=true");
        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView("/dashboard/income/add?error=true");
        }
    }

    @GetMapping("/transaction/{id}")
    public String transactionDetail(@PathVariable Long id, Model model, Authentication authentication) {
        int year = LocalDateTime.now().getYear();
        int month = LocalDateTime.now().getMonthValue();
        prepareDashboardModel(model, authentication, year, month);

        List<Transaction> all = transactionService.getTransactionsForCurrentUser();
        Optional<Transaction> tOpt = all.stream().filter(t -> t.getId().equals(id)).findFirst();

        if (tOpt.isPresent()) {
            model.addAttribute("transaction", tOpt.get());
            model.addAttribute("dateFormatted", tOpt.get().getDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")));
            auditLogRepository.save(new AuditLog(authentication.getName(), "VIEW_TRANSACTION", "Просмотр транзакции ID: " + id));
        } else {
            return "redirect:/dashboard/transactions";
        }
        return "transaction-detail";
    }

    @PostMapping("/update-limit")
    public RedirectView updateLimit(@RequestParam Long planId,
                                    @RequestParam BigDecimal newLimit,
                                    Authentication authentication) {
        Optional<BudgetPlan> planOpt = budgetPlanRepository.findById(planId);
        if (planOpt.isPresent()) {
            BudgetPlan plan = planOpt.get();
            plan.setMonthlyLimit(newLimit);
            budgetPlanRepository.save(plan);
            auditLogRepository.save(new AuditLog(authentication.getName(), "UPDATE_LIMIT", "Изменен лимит на " + newLimit));
        }
        return new RedirectView("/dashboard?success=true");
    }

    @PostMapping("/add-goal")
    public RedirectView addGoal(@RequestParam String name,
                                @RequestParam BigDecimal target,
                                @RequestParam(required = false) Long ownerId, // ID владельца, если цель личная
                                @RequestParam(required = false) String goalTypeStr, // FAMILY или PERSONAL
                                Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        SavingsGoal goal;
        SavingsGoal.GoalType type = SavingsGoal.GoalType.FAMILY;

        if ("PERSONAL".equalsIgnoreCase(goalTypeStr) && ownerId != null) {
            User owner = userRepository.findById(ownerId).orElseThrow();
            // Проверка: личный владелец должен быть из той же семьи
            if (owner.getFamily().getId().equals(user.getFamily().getId())) {
                goal = new SavingsGoal(name, target, user.getFamily(), owner);
                type = SavingsGoal.GoalType.PERSONAL;
            } else {
                // Если владелец не из семьи, создаем как семейную
                goal = new SavingsGoal(name, target, user.getFamily());
            }
        } else {
            goal = new SavingsGoal(name, target, user.getFamily());
        }

        goalRepository.save(goal);
        auditLogRepository.save(new AuditLog(username, "CREATE_GOAL", "Создана цель: " + name + " (Тип: " + type + ")"));
        return new RedirectView("/dashboard?success=true");
    }

    @PostMapping("/goal/{goalId}/contribute")
    public RedirectView contributeToGoal(@PathVariable Long goalId,
                                         @RequestParam BigDecimal amount,
                                         Authentication authentication) {
        Optional<SavingsGoal> goalOpt = goalRepository.findById(goalId);
        if (goalOpt.isPresent()) {
            SavingsGoal goal = goalOpt.get();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElseThrow();

            boolean canContribute = false;

            // Проверка прав:
            // 1. Если цель семейная - может любой член семьи
            // 2. Если цель личная - только владелец или глава семьи (опционально)
            if (goal.getGoalType() == SavingsGoal.GoalType.FAMILY) {
                if (goal.getFamily().getId().equals(user.getFamily().getId())) {
                    canContribute = true;
                }
            } else if (goal.getGoalType() == SavingsGoal.GoalType.PERSONAL) {
                if (goal.getOwner().getId().equals(user.getId())) {
                    canContribute = true;
                }
            }

            if (canContribute) {
                goal.contribute(amount);
                goalRepository.save(goal);

                auditLogRepository.save(new AuditLog(
                        authentication.getName(),
                        "CONTRIBUTE_TO_GOAL",
                        "Внесено " + amount + " ₽ в цель: " + goal.getName()
                ));
            }
        }
        return new RedirectView("/dashboard?success=true");
    }

    @PostMapping("/goal/{goalId}/delete")
    public RedirectView deleteGoal(@PathVariable Long goalId, Authentication authentication) {
        Optional<SavingsGoal> goalOpt = goalRepository.findById(goalId);
        if (goalOpt.isPresent()) {
            SavingsGoal goal = goalOpt.get();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElseThrow();

            boolean canDelete = false;

            // Удалять может только владелец личной цели или Глава семьи (для любых целей в своей семье)
            if (user.getRole() == com.familybudget.model.Role.OWNER && goal.getFamily().getId().equals(user.getFamily().getId())) {
                canDelete = true;
            } else if (goal.getGoalType() == SavingsGoal.GoalType.PERSONAL && goal.getOwner().getId().equals(user.getId())) {
                canDelete = true;
            }

            if (canDelete) {
                goalRepository.delete(goal);
                auditLogRepository.save(new AuditLog(
                        authentication.getName(),
                        "DELETE_GOAL",
                        "Удалена цель: " + goal.getName()
                ));
            }
        }
        return new RedirectView("/dashboard?success=true");
    }

    private void prepareDashboardModel(Model model, Authentication authentication, int year, int month) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        LocalDateTime startPeriod = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endPeriod = startPeriod.plusMonths(1);
        LocalDateTime prevMonthStart = startPeriod.minusMonths(1);

        List<Transaction> allTransactions = transactionService.getTransactionsForCurrentUser();
        List<Transaction> periodTransactions = allTransactions.stream()
                .filter(t -> !t.getDate().isBefore(startPeriod) && t.getDate().isBefore(endPeriod))
                .collect(Collectors.toList());

        BigDecimal periodSpent = periodTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Income> allIncomes = incomeRepository.findAll();
        BigDecimal currentIncome = allIncomes.stream()
                .filter(i -> i.getUser().getFamily().getId().equals(user.getFamily().getId())
                        && !i.getDate().isBefore(startPeriod) && i.getDate().isBefore(endPeriod))
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lastMonthIncome = allIncomes.stream()
                .filter(i -> i.getUser().getFamily().getId().equals(user.getFamily().getId())
                        && !i.getDate().isBefore(prevMonthStart) && i.getDate().isBefore(startPeriod))
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String savingsStatus = "Нет данных за прошлый период";
        double savingsPercent = 0;
        if (lastMonthIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsPercent = ((currentIncome.doubleValue() - lastMonthIncome.doubleValue()) / lastMonthIncome.doubleValue()) * 100;
            savingsStatus = (savingsPercent > 0 ? "📈 Вырос на " : "📉 Упал на ")
                    + String.format("%.1f", Math.abs(savingsPercent)) + "%";
        }

        BudgetPlan plan = budgetPlanRepository.findByFamilyId(user.getFamily().getId())
                .orElse(new BudgetPlan(user.getFamily(), BigDecimal.ZERO, user));
        BigDecimal limit = plan.getMonthlyLimit() != null ? plan.getMonthlyLimit() : BigDecimal.ZERO;
        double percent = limit.compareTo(BigDecimal.ZERO) > 0
                ? (periodSpent.doubleValue() / limit.doubleValue()) * 100
                : 0;

        Map<String, Double> categoryData = periodTransactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(),
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())));

        try {
            ObjectMapper mapper = new ObjectMapper();
            model.addAttribute("chartLabelsJson", mapper.writeValueAsString(new ArrayList<>(categoryData.keySet())));
            model.addAttribute("chartDataJson", mapper.writeValueAsString(new ArrayList<>(categoryData.values())));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("chartLabelsJson", "[]");
            model.addAttribute("chartDataJson", "[]");
        }

        List<Transaction> recentTransactions = periodTransactions.stream().limit(5).collect(Collectors.toList());

        model.addAttribute("monthSpent", periodSpent);
        model.addAttribute("currentIncome", currentIncome);
        model.addAttribute("savingsStatus", savingsStatus);
        model.addAttribute("limit", limit);
        model.addAttribute("percent", Math.min(percent, 100));
        model.addAttribute("transactionCount", periodTransactions.size());
        model.addAttribute("userRole", user.getRole().getTitle());
        model.addAttribute("planId", plan.getId());
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("userName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
    }
}