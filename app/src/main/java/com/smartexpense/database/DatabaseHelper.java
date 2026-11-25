package com.smartexpense.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smartexpense.db";
    private static final int DATABASE_VERSION = 1;

    // Table: expenses
    public static final String TABLE_EXPENSES = "expenses";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "expense_name";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_CATEGORY = "category";
    public static final String COL_PAYMENT_METHOD = "payment_method";
    public static final String COL_DATE = "expense_date";
    public static final String COL_NOTES = "notes";
    public static final String COL_CREATED_AT = "created_at";

    // Table: category_budgets
    public static final String TABLE_CATEGORY_BUDGETS = "category_budgets";
    public static final String COL_BUDGET_ID = "id";
    public static final String COL_BUDGET_CATEGORY = "category";
    public static final String COL_BUDGET_AMOUNT = "budget_amount";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_CATEGORY + " TEXT NOT NULL, " +
                COL_PAYMENT_METHOD + " TEXT NOT NULL, " +
                COL_DATE + " INTEGER NOT NULL, " +
                COL_NOTES + " TEXT, " +
                COL_CREATED_AT + " INTEGER NOT NULL" +
                ");";

        String createIndexDate = "CREATE INDEX idx_expenses_date ON " + TABLE_EXPENSES + "(" + COL_DATE + ");";
        String createIndexCategory = "CREATE INDEX idx_expenses_category ON " + TABLE_EXPENSES + "(" + COL_CATEGORY + ");";

        String createBudgetsTable = "CREATE TABLE " + TABLE_CATEGORY_BUDGETS + " (" +
                COL_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BUDGET_CATEGORY + " TEXT UNIQUE NOT NULL, " +
                COL_BUDGET_AMOUNT + " REAL NOT NULL" +
                ");";

        db.execSQL(createExpensesTable);
        db.execSQL(createIndexDate);
        db.execSQL(createIndexCategory);
        db.execSQL(createBudgetsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY_BUDGETS);
        onCreate(db);
    }
}
