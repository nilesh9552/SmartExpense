package com.smartexpense.repositories;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.smartexpense.database.ExpenseDao;
import com.smartexpense.models.CategoryBudget;
import com.smartexpense.models.CategorySummary;
import com.smartexpense.models.Expense;
import com.smartexpense.models.MonthlyComparison;
import com.smartexpense.utils.DateUtils;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {

    public interface RepositoryCallback<T> {
        void onComplete(T result);
    }

    private static ExpenseRepository instance;
    private final ExpenseDao expenseDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    private final MutableLiveData<List<Expense>> allExpensesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Expense>> recentExpensesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> todaySpendingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> monthlySpendingLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CategorySummary>> monthlyCategoriesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Expense>> filteredExpensesLiveData = new MutableLiveData<>();

    public static synchronized ExpenseRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ExpenseRepository(context.getApplicationContext());
        }
        return instance;
    }

    private ExpenseRepository(Context context) {
        this.expenseDao = new ExpenseDao(context);
        this.executor = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
        refreshDashboardData();
        loadAllExpenses();
    }

    public LiveData<List<Expense>> getAllExpensesLiveData() {
        return allExpensesLiveData;
    }

    public LiveData<List<Expense>> getRecentExpensesLiveData() {
        return recentExpensesLiveData;
    }

    public LiveData<Double> getTodaySpendingLiveData() {
        return todaySpendingLiveData;
    }

    public LiveData<Double> getMonthlySpendingLiveData() {
        return monthlySpendingLiveData;
    }

    public LiveData<List<CategorySummary>> getMonthlyCategoriesLiveData() {
        return monthlyCategoriesLiveData;
    }

    public LiveData<List<Expense>> getFilteredExpensesLiveData() {
        return filteredExpensesLiveData;
    }

    public void insertExpense(Expense expense, RepositoryCallback<Long> callback) {
        executor.execute(() -> {
            long id = expenseDao.insertExpense(expense);
            mainHandler.post(() -> {
                if (callback != null) callback.onComplete(id);
                refreshDashboardData();
                loadAllExpenses();
            });
        });
    }

    public void updateExpense(Expense expense, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            int rows = expenseDao.updateExpense(expense);
            mainHandler.post(() -> {
                if (callback != null) callback.onComplete(rows);
                refreshDashboardData();
                loadAllExpenses();
            });
        });
    }

    public void deleteExpense(long id, RepositoryCallback<Integer> callback) {
        executor.execute(() -> {
            int rows = expenseDao.deleteExpense(id);
            mainHandler.post(() -> {
                if (callback != null) callback.onComplete(rows);
                refreshDashboardData();
                loadAllExpenses();
            });
        });
    }

    public void getExpenseById(long id, RepositoryCallback<Expense> callback) {
        executor.execute(() -> {
            Expense expense = expenseDao.getExpenseById(id);
            mainHandler.post(() -> {
                if (callback != null) callback.onComplete(expense);
            });
        });
    }

    public void refreshDashboardData() {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            long monthStart = DateUtils.getStartOfMonth(now);
            long monthEnd = DateUtils.getEndOfMonth(now);

            double todaySpent = expenseDao.getTodayTotal();
            double monthSpent = expenseDao.getTotalSpentBetween(monthStart, monthEnd);
            List<Expense> recent = expenseDao.getRecentExpenses(5);
            List<CategorySummary> summaries = expenseDao.getCategorySummaries(monthStart, monthEnd);

            mainHandler.post(() -> {
                todaySpendingLiveData.setValue(todaySpent);
                monthlySpendingLiveData.setValue(monthSpent);
                recentExpensesLiveData.setValue(recent);
                monthlyCategoriesLiveData.setValue(summaries);
            });
        });
    }

    public void loadAllExpenses() {
        executor.execute(() -> {
            List<Expense> expenses = expenseDao.getAllExpenses();
            mainHandler.post(() -> allExpensesLiveData.setValue(expenses));
        });
    }

    public void filterExpenses(String query, String category, long startDate, long endDate) {
        executor.execute(() -> {
            List<Expense> filtered = expenseDao.getFilteredExpenses(query, category, startDate, endDate);
            mainHandler.post(() -> filteredExpensesLiveData.setValue(filtered));
        });
    }

    public void getAnalyticsData(long startDate, long endDate, RepositoryCallback<AnalyticsResult> callback) {
        executor.execute(() -> {
            double totalSpent = expenseDao.getTotalSpentBetween(startDate, endDate);
            List<CategorySummary> summaries = expenseDao.getCategorySummaries(startDate, endDate);
            Map<String, Double> dailySpending = expenseDao.getDailySpending(startDate, endDate);
            List<Expense> expenses = expenseDao.getFilteredExpenses(null, null, startDate, endDate);

            // Calculate previous month comparison
            long now = System.currentTimeMillis();
            long currMonthStart = DateUtils.getStartOfMonth(now);
            long currMonthEnd = DateUtils.getEndOfMonth(now);
            double currMonthTotal = expenseDao.getTotalSpentBetween(currMonthStart, currMonthEnd);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(now);
            cal.add(Calendar.MONTH, -1);
            long prevMonthStart = DateUtils.getStartOfMonth(cal.getTimeInMillis());
            long prevMonthEnd = DateUtils.getEndOfMonth(cal.getTimeInMillis());
            double prevMonthTotal = expenseDao.getTotalSpentBetween(prevMonthStart, prevMonthEnd);

            MonthlyComparison comparison = new MonthlyComparison(currMonthTotal, prevMonthTotal);

            AnalyticsResult result = new AnalyticsResult(totalSpent, summaries, dailySpending, expenses, comparison);
            mainHandler.post(() -> {
                if (callback != null) callback.onComplete(result);
            });
        });
    }

    public void getCategoryBudgets(RepositoryCallback<List<CategoryBudget>> callback) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();
            long monthStart = DateUtils.getStartOfMonth(now);
            long monthEnd = DateUtils.getEndOfMonth(now);
            List<CategoryBudget> list = expenseDao.getAllCategoryBudgets(monthStart, monthEnd);
            mainHandler.post(() -> {
                if (callback != null) callback.onComplete(list);
            });
        });
    }

    public void setCategoryBudget(String category, double amount, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            expenseDao.setCategoryBudget(category, amount);
            mainHandler.post(() -> {
                if (callback != null) callback.onComplete(null);
            });
        });
    }

    public static class AnalyticsResult {
        public final double totalSpent;
        public final List<CategorySummary> summaries;
        public final Map<String, Double> dailySpending;
        public final List<Expense> expenses;
        public final MonthlyComparison monthlyComparison;

        public AnalyticsResult(double totalSpent, List<CategorySummary> summaries,
                               Map<String, Double> dailySpending, List<Expense> expenses,
                               MonthlyComparison monthlyComparison) {
            this.totalSpent = totalSpent;
            this.summaries = summaries;
            this.dailySpending = dailySpending;
            this.expenses = expenses;
            this.monthlyComparison = monthlyComparison;
        }
    }
}
