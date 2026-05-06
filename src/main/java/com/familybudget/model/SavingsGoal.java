package com.familybudget.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "savings_goals")
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal targetAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    // Тип цели: FAMILY (общая) или PERSONAL (личная)
    @Enumerated(EnumType.STRING)
    private GoalType goalType = GoalType.FAMILY;

    // Владелец цели (если цель личная). Если семейная - null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    public enum GoalType {
        FAMILY, PERSONAL
    }

    public SavingsGoal() {
    }

    public SavingsGoal(String name, BigDecimal targetAmount, Family family) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.family = family;
        this.goalType = GoalType.FAMILY;
    }

    public SavingsGoal(String name, BigDecimal targetAmount, Family family, User owner) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.family = family;
        this.owner = owner;
        this.goalType = GoalType.PERSONAL;
    }

    // Метод для внесения денег в цель
    public void contribute(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.currentAmount = this.currentAmount.add(amount);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public void setGoalType(GoalType goalType) {
        this.goalType = goalType;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
    }

    // Вычисление процента выполнения
    public int getPercent() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return (int) ((currentAmount.doubleValue() / targetAmount.doubleValue()) * 100);
    }
}