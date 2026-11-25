package com.smartexpense.models;

import java.io.Serializable;

/**
 * Model representing category aggregated spending.
 */
public class CategorySummary implements Serializable {
    private String category;
    private double totalAmount;
    private int count;
    private float percentage;

    public CategorySummary(String category, double totalAmount, int count, float percentage) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.count = count;
        this.percentage = percentage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
