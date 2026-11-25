package com.smartexpense.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "smart_expense_preferences";
    private static final String KEY_MONTHLY_BUDGET = "key_monthly_budget";
    private static final String KEY_CURRENCY_CODE = "key_currency_code";
    private static final String KEY_CURRENCY_SYMBOL = "key_currency_symbol";
    private static final String KEY_DARK_MODE = "key_dark_mode";
    private static final String KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled";
    private static final String KEY_FIRST_LAUNCH = "key_first_launch";

    public static final int MODE_SYSTEM = 0;
    public static final int MODE_LIGHT = 1;
    public static final int MODE_DARK = 2;

    private final SharedPreferences preferences;

    public PreferenceManager(Context context) {
        this.preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public double getMonthlyBudget() {
        return Double.longBitsToDouble(preferences.getLong(KEY_MONTHLY_BUDGET, Double.doubleToLongBits(2000.0)));
    }

    public void setMonthlyBudget(double budget) {
        preferences.edit().putLong(KEY_MONTHLY_BUDGET, Double.doubleToLongBits(budget)).apply();
    }

    public String getCurrencyCode() {
        return preferences.getString(KEY_CURRENCY_CODE, "USD");
    }

    public String getCurrencySymbol() {
        return preferences.getString(KEY_CURRENCY_SYMBOL, "$");
    }

    public void setCurrency(String code, String symbol) {
        preferences.edit()
                .putString(KEY_CURRENCY_CODE, code)
                .putString(KEY_CURRENCY_SYMBOL, symbol)
                .apply();
    }

    public int getDarkMode() {
        return preferences.getInt(KEY_DARK_MODE, MODE_SYSTEM);
    }

    public void setDarkMode(int mode) {
        preferences.edit().putInt(KEY_DARK_MODE, mode).apply();
    }

    public boolean areNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean isFirstLaunch() {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchCompleted() {
        preferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }
}
