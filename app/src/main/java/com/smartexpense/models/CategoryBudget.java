package com.smartexpense.models;

import java.io.Serializable;

/**
 * Model representing budget allocation and spent amount for a specific category.
 */
public class CategoryBudget implements Serializable {
    private long id;
    private String category;
    private double budgetAmount;
    private double spentAmount;

    public CategoryBudget(long id, String category, double budgetAmount, double spentAmount) {
        this.id = id;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
    }

    public CategoryBudget(String category, double budgetAmount, double spentAmount) {
        this(0, category, budgetAmount, spentAmount);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public int getPercentageUsed() {
        if (budgetAmount <= 0) return 0;
        int pct = (int) Math.round((spentAmount / budgetAmount) * 100);
        return Math.min(pct, 100);
    }
}
