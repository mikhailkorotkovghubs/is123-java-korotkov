package com.familybudget.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(unique = true, nullable = false) private String username; // Логин
    private String email;
    private String passwordHash;

    @Enumerated(EnumType.STRING) private Role role; // OWNER, MEMBER

    private String displayName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    @JsonIgnore
    private Family family;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BankCard> cards = new HashSet<>();

    public User() {}
    public User(String username, String email, String passwordHash, Role role) {
        this.username = username; this.email = email; this.passwordHash = passwordHash; this.role = role;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; } public void setRole(Role role) { this.role = role; }
    public String getDisplayName() { return displayName; } public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Family getFamily() { return family; } public void setFamily(Family family) { this.family = family; }
    public Set<BankCard> getCards() { return cards; } public void setCards(Set<BankCard> cards) { this.cards = cards; }
}