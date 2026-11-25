package com.smartexpense;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.smartexpense.database.ExpenseDao;
import com.smartexpense.models.Expense;
import com.smartexpense.utils.NotificationUtils;
import com.smartexpense.utils.PreferenceManager;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class SmartExpenseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceManager prefManager = new PreferenceManager(this);

        // Apply dark mode preference
        int mode = prefManager.getDarkMode();
        if (mode == PreferenceManager.MODE_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (mode == PreferenceManager.MODE_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        // Initialize notification channel
        NotificationUtils.createNotificationChannel(this);

        // Seed initial sample expenses on first launch
        if (prefManager.isFirstLaunch()) {
            seedSampleData(prefManager);
        }
    }

    private void seedSampleData(PreferenceManager prefManager) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ExpenseDao dao = new ExpenseDao(this);

            long now = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();

            // 1. Food today
            dao.insertExpense(new Expense("Coffee & Bakery", 12.50, "Food", "Cash", now - (1000 * 60 * 60 * 2), "Morning breakfast"));

            // 2. Groceries today
            dao.insertExpense(new Expense("Supermarket Groceries", 78.40, "Food", "UPI", now - (1000 * 60 * 60 * 5), "Weekly fruits, vegetables and milk"));

            // 3. Transport yesterday
            cal.setTimeInMillis(now);
            cal.add(Calendar.DAY_OF_YEAR, -1);
            dao.insertExpense(new Expense("Metro Card Recharge", 35.00, "Transport", "Debit Card", cal.getTimeInMillis(), "Commute card"));

            // 4. Bills 3 days ago
            cal.setTimeInMillis(now);
            cal.add(Calendar.DAY_OF_YEAR, -3);
            dao.insertExpense(new Expense("Electricity & Water Bill", 115.00, "Bills", "Bank Transfer", cal.getTimeInMillis(), "Monthly utilities"));

            // 5. Shopping 5 days ago
            cal.setTimeInMillis(now);
            cal.add(Calendar.DAY_OF_YEAR, -5);
            dao.insertExpense(new Expense("Casual Running Shoes", 89.99, "Shopping", "Credit Card", cal.getTimeInMillis(), "Sport shoes on discount"));

            // 6. Health 7 days ago
            cal.setTimeInMillis(now);
            cal.add(Calendar.DAY_OF_YEAR, -7);
            dao.insertExpense(new Expense("Pharmacy & Vitamins", 42.50, "Health", "UPI", cal.getTimeInMillis(), "Monthly vitamins and supplements"));

            // 7. Education 9 days ago
            cal.setTimeInMillis(now);
            cal.add(Calendar.DAY_OF_YEAR, -9);
            dao.insertExpense(new Expense("Programming Course", 29.99, "Education", "Credit Card", cal.getTimeInMillis(), "Online certification"));

            // 8. Entertainment 12 days ago
            cal.setTimeInMillis(now);
            cal.add(Calendar.DAY_OF_YEAR, -12);
            dao.insertExpense(new Expense("Movie Night & Snacks", 32.00, "Entertainment", "UPI", cal.getTimeInMillis(), "Weekend cinema"));

            // Initialize default category budgets
            dao.setCategoryBudget("Food", 450.0);
            dao.setCategoryBudget("Transport", 150.0);
            dao.setCategoryBudget("Shopping", 300.0);
            dao.setCategoryBudget("Bills", 350.0);
            dao.setCategoryBudget("Health", 150.0);
            dao.setCategoryBudget("Education", 100.0);
            dao.setCategoryBudget("Entertainment", 120.0);
            dao.setCategoryBudget("Other", 100.0);

            prefManager.setFirstLaunchCompleted();
        });
    }
}
