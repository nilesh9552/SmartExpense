package com.smartexpense.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.smartexpense.models.CategoryBudget;
import com.smartexpense.models.CategorySummary;
import com.smartexpense.models.Expense;
import com.smartexpense.utils.DateUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpenseDao {

    private final DatabaseHelper dbHelper;

    public ExpenseDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, expense.getName());
        values.put(DatabaseHelper.COL_AMOUNT, expense.getAmount());
        values.put(DatabaseHelper.COL_CATEGORY, expense.getCategory());
        values.put(DatabaseHelper.COL_PAYMENT_METHOD, expense.getPaymentMethod());
        values.put(DatabaseHelper.COL_DATE, expense.getDate());
        values.put(DatabaseHelper.COL_NOTES, expense.getNotes());
        values.put(DatabaseHelper.COL_CREATED_AT, expense.getCreatedAt() > 0 ? expense.getCreatedAt() : System.currentTimeMillis());

        return db.insert(DatabaseHelper.TABLE_EXPENSES, null, values);
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, expense.getName());
        values.put(DatabaseHelper.COL_AMOUNT, expense.getAmount());
        values.put(DatabaseHelper.COL_CATEGORY, expense.getCategory());
        values.put(DatabaseHelper.COL_PAYMENT_METHOD, expense.getPaymentMethod());
        values.put(DatabaseHelper.COL_DATE, expense.getDate());
        values.put(DatabaseHelper.COL_NOTES, expense.getNotes());

        return db.update(
                DatabaseHelper.TABLE_EXPENSES,
                values,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(expense.getId())}
        );
    }

    public int deleteExpense(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseHelper.TABLE_EXPENSES,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public Expense getExpenseById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_EXPENSES,
                null,
                DatabaseHelper.COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Expense expense = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                expense = cursorToExpense(cursor);
            }
            cursor.close();
        }
        return expense;
    }

    public List<Expense> getAllExpenses() {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_EXPENSES,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COL_DATE + " DESC, " + DatabaseHelper.COL_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToExpense(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Expense> getRecentExpenses(int limit) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_EXPENSES,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COL_DATE + " DESC, " + DatabaseHelper.COL_ID + " DESC",
                String.valueOf(limit)
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToExpense(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Expense> getFilteredExpenses(String query, String category, long startDate, long endDate) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            selection.append("(").append(DatabaseHelper.COL_NAME).append(" LIKE ? OR ")
                    .append(DatabaseHelper.COL_NOTES).append(" LIKE ?)");
            selectionArgs.add("%" + query.trim() + "%");
            selectionArgs.add("%" + query.trim() + "%");
        }

        if (category != null && !category.equalsIgnoreCase("All")) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(DatabaseHelper.COL_CATEGORY).append(" = ?");
            selectionArgs.add(category);
        }

        if (startDate > 0 && endDate >= startDate) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(DatabaseHelper.COL_DATE).append(" >= ? AND ")
                    .append(DatabaseHelper.COL_DATE).append(" <= ?");
            selectionArgs.add(String.valueOf(startDate));
            selectionArgs.add(String.valueOf(endDate));
        }

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_EXPENSES,
                null,
                selection.length() > 0 ? selection.toString() : null,
                selectionArgs.size() > 0 ? selectionArgs.toArray(new String[0]) : null,
                null,
                null,
                DatabaseHelper.COL_DATE + " DESC, " + DatabaseHelper.COL_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToExpense(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public double getTotalSpentBetween(long startDate, long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT SUM(" + DatabaseHelper.COL_AMOUNT + ") FROM " + DatabaseHelper.TABLE_EXPENSES +
                " WHERE " + DatabaseHelper.COL_DATE + " >= ? AND " + DatabaseHelper.COL_DATE + " <= ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startDate), String.valueOf(endDate)});
        double total = 0.0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        return total;
    }

    public double getTodayTotal() {
        long now = System.currentTimeMillis();
        long startOfDay = DateUtils.getStartOfDay(now);
        long endOfDay = DateUtils.getEndOfDay(now);
        return getTotalSpentBetween(startOfDay, endOfDay);
    }

    public List<CategorySummary> getCategorySummaries(long startDate, long endDate) {
        List<CategorySummary> list = new ArrayList<>();
        double grandTotal = getTotalSpentBetween(startDate, endDate);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DatabaseHelper.COL_CATEGORY + ", SUM(" + DatabaseHelper.COL_AMOUNT + "), COUNT(" + DatabaseHelper.COL_ID + ") " +
                "FROM " + DatabaseHelper.TABLE_EXPENSES + " " +
                "WHERE " + DatabaseHelper.COL_DATE + " >= ? AND " + DatabaseHelper.COL_DATE + " <= ? " +
                "GROUP BY " + DatabaseHelper.COL_CATEGORY + " " +
                "ORDER BY SUM(" + DatabaseHelper.COL_AMOUNT + ") DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startDate), String.valueOf(endDate)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String cat = cursor.getString(0);
                double total = cursor.getDouble(1);
                int count = cursor.getInt(2);
                float pct = grandTotal > 0 ? (float) ((total / grandTotal) * 100.0) : 0f;
                list.add(new CategorySummary(cat, total, count, pct));
            }
            cursor.close();
        }
        return list;
    }

    public Map<String, Double> getDailySpending(long startDate, long endDate) {
        Map<String, Double> map = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " + DatabaseHelper.COL_DATE + ", " + DatabaseHelper.COL_AMOUNT + " " +
                "FROM " + DatabaseHelper.TABLE_EXPENSES + " " +
                "WHERE " + DatabaseHelper.COL_DATE + " >= ? AND " + DatabaseHelper.COL_DATE + " <= ? " +
                "ORDER BY " + DatabaseHelper.COL_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(startDate), String.valueOf(endDate)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long date = cursor.getLong(0);
                double amount = cursor.getDouble(1);
                String dayLabel = DateUtils.formatDayShort(date);
                Double current = map.get(dayLabel);
                map.put(dayLabel, (current != null ? current : 0.0) + amount);
            }
            cursor.close();
        }
        return map;
    }

    // Category budget methods
    public void setCategoryBudget(String category, double amount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BUDGET_CATEGORY, category);
        values.put(DatabaseHelper.COL_BUDGET_AMOUNT, amount);

        db.insertWithOnConflict(
                DatabaseHelper.TABLE_CATEGORY_BUDGETS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public double getCategoryBudget(String category) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CATEGORY_BUDGETS,
                new String[]{DatabaseHelper.COL_BUDGET_AMOUNT},
                DatabaseHelper.COL_BUDGET_CATEGORY + " = ?",
                new String[]{category},
                null, null, null
        );

        double budget = 0.0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                budget = cursor.getDouble(0);
            }
            cursor.close();
        }
        return budget;
    }

    public List<CategoryBudget> getAllCategoryBudgets(long monthStart, long monthEnd) {
        String[] defaultCategories = new String[]{
                "Food", "Transport", "Shopping", "Bills", "Health", "Education", "Entertainment", "Other"
        };

        List<CategoryBudget> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        for (String cat : defaultCategories) {
            double budgetLimit = getCategoryBudget(cat);
            if (budgetLimit <= 0) {
                // Default reasonable fallback
                budgetLimit = 300.0;
            }

            // Calculate spent amount in current month for this category
            String query = "SELECT SUM(" + DatabaseHelper.COL_AMOUNT + ") FROM " + DatabaseHelper.TABLE_EXPENSES +
                    " WHERE " + DatabaseHelper.COL_CATEGORY + " = ? AND " +
                    DatabaseHelper.COL_DATE + " >= ? AND " + DatabaseHelper.COL_DATE + " <= ?";
            Cursor cursor = db.rawQuery(query, new String[]{cat, String.valueOf(monthStart), String.valueOf(monthEnd)});
            double spent = 0.0;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    spent = cursor.getDouble(0);
                }
                cursor.close();
            }

            list.add(new CategoryBudget(cat, budgetLimit, spent));
        }

        return list;
    }

    private Expense cursorToExpense(Cursor cursor) {
        Expense expense = new Expense();
        expense.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
        expense.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)));
        expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT)));
        expense.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY)));
        expense.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PAYMENT_METHOD)));
        expense.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE)));
        expense.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTES)));
        expense.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CREATED_AT)));
        return expense;
    }
}
