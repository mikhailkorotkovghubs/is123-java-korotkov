package com.familybudget.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore; // <--- 1. Добавь этот импорт

@Entity
@Table(name = "bank_cards")
public class BankCard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    private String cardNumberMasked; // Например: **** 1234
    private String bankName;
    private String ownerName;        // Имя на карте

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User owner;

    public BankCard() {}
    public BankCard(String cardNumberMasked, String bankName, String ownerName, User owner) {
        this.cardNumberMasked = cardNumberMasked;
        this.bankName = bankName;
        this.ownerName = ownerName;
        this.owner = owner;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getCardNumberMasked() { return cardNumberMasked; } public void setCardNumberMasked(String cardNumberMasked) { this.cardNumberMasked = cardNumberMasked; }
    public String getBankName() { return bankName; } public void setBankName(String bankName) { this.bankName = bankName; }
    public String getOwnerName() { return ownerName; } public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public User getOwner() { return owner; } public void setOwner(User owner) { this.owner = owner; }
}