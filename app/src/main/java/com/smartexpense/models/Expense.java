package com.smartexpense.models;

import java.io.Serializable;

/**
 * Model representing a single expense transaction.
 */
public class Expense implements Serializable {
    private long id;
    private String name;
    private double amount;
    private String category;
    private String paymentMethod;
    private long date;
    private String notes;
    private long createdAt;

    public Expense() {
    }

    public Expense(String name, double amount, String category, String paymentMethod, long date, String notes) {
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.date = date;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }

    public Expense(long id, String name, double amount, String category, String paymentMethod, long date, String notes, long createdAt) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.date = date;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
