package com.familybudget.model;

public enum Role {
    OWNER("Владелец", "Полный доступ, управление бюджетом"),
    ACCOUNTANT("Бухгалтер", "Добавление расходов, просмотр отчетов"),
    MEMBER("Участник", "Только просмотр своих расходов"),
    GUEST("Гость", "Только просмотр общей статистики");

    private final String title;
    private final String description;

    Role(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
}