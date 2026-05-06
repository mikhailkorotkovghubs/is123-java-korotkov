package com.familybudget.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private LocalDateTime date;
    private String description;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "category_id", nullable = false) private Category category;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;

    public Transaction() { this.date = LocalDateTime.now(); }
    public Transaction(BigDecimal amount, Category category, User user, String description) {
        this.amount = amount; this.category = category; this.user = user; this.description = description; this.date = LocalDateTime.now();
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getDate() { return date; } public void setDate(LocalDateTime date) { this.date = date; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; } public void setCategory(Category category) { this.category = category; }
    public User getUser() { return user; } public void setUser(User user) { this.user = user; }
}