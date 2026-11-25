package com.smartexpense.models;

import java.io.Serializable;

/**
 * Model representing monthly comparative metrics.
 */
public class MonthlyComparison implements Serializable {
    private final double currentMonthTotal;
    private final double previousMonthTotal;
    private final double difference;
    private final double percentageChange;
    private final boolean isIncrease;

    public MonthlyComparison(double currentMonthTotal, double previousMonthTotal) {
        this.currentMonthTotal = currentMonthTotal;
        this.previousMonthTotal = previousMonthTotal;
        this.difference = Math.abs(currentMonthTotal - previousMonthTotal);

        if (previousMonthTotal > 0) {
            this.percentageChange = ((currentMonthTotal - previousMonthTotal) / previousMonthTotal) * 100.0;
        } else {
            this.percentageChange = currentMonthTotal > 0 ? 100.0 : 0.0;
        }
        this.isIncrease = currentMonthTotal >= previousMonthTotal;
    }

    public double getCurrentMonthTotal() {
        return currentMonthTotal;
    }

    public double getPreviousMonthTotal() {
        return previousMonthTotal;
    }

    public double getDifference() {
        return difference;
    }

    public double getPercentageChange() {
        return percentageChange;
    }

    public boolean isIncrease() {
        return isIncrease;
    }
}
