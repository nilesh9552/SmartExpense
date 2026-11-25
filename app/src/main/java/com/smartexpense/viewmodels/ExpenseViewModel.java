package com.smartexpense.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.smartexpense.models.CategoryBudget;
import com.smartexpense.models.CategorySummary;
import com.smartexpense.models.Expense;
import com.smartexpense.repositories.ExpenseRepository;
import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        this.repository = ExpenseRepository.getInstance(application);
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return repository.getAllExpensesLiveData();
    }

    public LiveData<List<Expense>> getRecentExpenses() {
        return repository.getRecentExpensesLiveData();
    }

    public LiveData<Double> getTodaySpending() {
        return repository.getTodaySpendingLiveData();
    }

    public LiveData<Double> getMonthlySpending() {
        return repository.getMonthlySpendingLiveData();
    }

    public LiveData<List<CategorySummary>> getMonthlyCategories() {
        return repository.getMonthlyCategoriesLiveData();
    }

    public LiveData<List<Expense>> getFilteredExpenses() {
        return repository.getFilteredExpensesLiveData();
    }

    public void insertExpense(Expense expense, ExpenseRepository.RepositoryCallback<Long> callback) {
        repository.insertExpense(expense, callback);
    }

    public void updateExpense(Expense expense, ExpenseRepository.RepositoryCallback<Integer> callback) {
        repository.updateExpense(expense, callback);
    }

    public void deleteExpense(long id, ExpenseRepository.RepositoryCallback<Integer> callback) {
        repository.deleteExpense(id, callback);
    }

    public void getExpenseById(long id, ExpenseRepository.RepositoryCallback<Expense> callback) {
        repository.getExpenseById(id, callback);
    }

    public void refreshDashboardData() {
        repository.refreshDashboardData();
    }

    public void loadAllExpenses() {
        repository.loadAllExpenses();
    }

    public void filterExpenses(String query, String category, long startDate, long endDate) {
        repository.filterExpenses(query, category, startDate, endDate);
    }

    public void getAnalyticsData(long startDate, long endDate, ExpenseRepository.RepositoryCallback<ExpenseRepository.AnalyticsResult> callback) {
        repository.getAnalyticsData(startDate, endDate, callback);
    }

    public void getCategoryBudgets(ExpenseRepository.RepositoryCallback<List<CategoryBudget>> callback) {
        repository.getCategoryBudgets(callback);
    }

    public void setCategoryBudget(String category, double amount, ExpenseRepository.RepositoryCallback<Void> callback) {
        repository.setCategoryBudget(category, amount, callback);
    }
}
