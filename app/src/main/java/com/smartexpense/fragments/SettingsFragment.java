package com.smartexpense.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.smartexpense.R;
import com.smartexpense.activities.CategoryBudgetActivity;
import com.smartexpense.models.Expense;
import com.smartexpense.utils.CsvExportHelper;
import com.smartexpense.utils.CurrencyHelper;
import com.smartexpense.utils.PreferenceManager;
import com.smartexpense.viewmodels.ExpenseViewModel;
import com.smartexpense.viewmodels.ExpenseViewModelFactory;
import java.io.File;
import java.util.List;

public class SettingsFragment extends Fragment {

    private PreferenceManager prefManager;
    private ExpenseViewModel viewModel;

    private LinearLayout itemCurrency;
    private TextView tvCurrentCurrency;
    private LinearLayout itemMonthlyBudget;
    private TextView tvCurrentMonthlyBudget;
    private LinearLayout itemCategoryBudgets;
    private LinearLayout itemDarkMode;
    private TextView tvDarkModeStatus;
    private LinearLayout itemExportCsv;
    private LinearLayout itemBackupRestore;
    private MaterialCardView cardAboutApp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefManager = new PreferenceManager(requireContext());
        viewModel = new ViewModelProvider(requireActivity(), new ExpenseViewModelFactory(requireActivity().getApplication())).get(ExpenseViewModel.class);

        initViews(view);
        displayCurrentSettings();
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayCurrentSettings();
    }

    private void initViews(View view) {
        itemCurrency = view.findViewById(R.id.item_pref_currency);
        tvCurrentCurrency = view.findViewById(R.id.tv_current_currency);
        itemMonthlyBudget = view.findViewById(R.id.item_pref_monthly_budget);
        tvCurrentMonthlyBudget = view.findViewById(R.id.tv_current_monthly_budget);
        itemCategoryBudgets = view.findViewById(R.id.item_pref_category_budgets);
        itemDarkMode = view.findViewById(R.id.item_pref_dark_mode);
        tvDarkModeStatus = view.findViewById(R.id.tv_dark_mode_status);
        itemExportCsv = view.findViewById(R.id.item_pref_export_csv);
        itemBackupRestore = view.findViewById(R.id.item_pref_backup_restore);
        cardAboutApp = view.findViewById(R.id.card_about_app);
    }

    private void displayCurrentSettings() {
        // Currency
        String currCode = prefManager.getCurrencyCode();
        String currSymbol = prefManager.getCurrencySymbol();
        tvCurrentCurrency.setText(currCode + " (" + currSymbol + ")");

        // Monthly Budget
        double budget = prefManager.getMonthlyBudget();
        tvCurrentMonthlyBudget.setText(CurrencyHelper.formatAmount(requireContext(), budget));

        // Dark Mode
        int mode = prefManager.getDarkMode();
        if (mode == PreferenceManager.MODE_LIGHT) {
            tvDarkModeStatus.setText("Light Mode");
        } else if (mode == PreferenceManager.MODE_DARK) {
            tvDarkModeStatus.setText("Dark Mode");
        } else {
            tvDarkModeStatus.setText("System Default");
        }
    }

    private void setupClickListeners() {
        itemCurrency.setOnClickListener(v -> showCurrencyDialog());
        itemMonthlyBudget.setOnClickListener(v -> showBudgetDialog());
        itemCategoryBudgets.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CategoryBudgetActivity.class);
            startActivity(intent);
        });
        itemDarkMode.setOnClickListener(v -> showDarkModeDialog());
        itemExportCsv.setOnClickListener(v -> exportExpensesCsv());
        itemBackupRestore.setOnClickListener(v -> showBackupRestoreDialog());
        cardAboutApp.setOnClickListener(v -> showAboutDialog());
    }

    private void showCurrencyDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_currency_picker, null);
        RadioGroup rg = dialogView.findViewById(R.id.rg_currencies);

        String currentCode = prefManager.getCurrencyCode();
        if (currentCode.equalsIgnoreCase("INR")) {
            rg.check(R.id.rb_curr_inr);
        } else if (currentCode.equalsIgnoreCase("EUR")) {
            rg.check(R.id.rb_curr_eur);
        } else if (currentCode.equalsIgnoreCase("GBP")) {
            rg.check(R.id.rb_curr_gbp);
        } else if (currentCode.equalsIgnoreCase("JPY")) {
            rg.check(R.id.rb_curr_jpy);
        } else if (currentCode.equalsIgnoreCase("CAD")) {
            rg.check(R.id.rb_curr_cad);
        } else if (currentCode.equalsIgnoreCase("AUD")) {
            rg.check(R.id.rb_curr_aud);
        } else {
            rg.check(R.id.rb_curr_usd);
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                    int selectedId = rg.getCheckedRadioButtonId();
                    String code = "USD";
                    String symbol = "$";

                    if (selectedId == R.id.rb_curr_inr) {
                        code = "INR";
                        symbol = "₹";
                    } else if (selectedId == R.id.rb_curr_eur) {
                        code = "EUR";
                        symbol = "€";
                    } else if (selectedId == R.id.rb_curr_gbp) {
                        code = "GBP";
                        symbol = "£";
                    } else if (selectedId == R.id.rb_curr_jpy) {
                        code = "JPY";
                        symbol = "¥";
                    } else if (selectedId == R.id.rb_curr_cad) {
                        code = "CAD";
                        symbol = "CA$";
                    } else if (selectedId == R.id.rb_curr_aud) {
                        code = "AUD";
                        symbol = "AU$";
                    }

                    prefManager.setCurrency(code, symbol);
                    displayCurrentSettings();
                    if (viewModel != null) viewModel.refreshDashboardData();
                    Toast.makeText(requireContext(), "Currency updated to " + code, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showBudgetDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_budget, null);
        EditText etAmount = dialogView.findViewById(R.id.et_dialog_budget_amount);

        double currentBudget = prefManager.getMonthlyBudget();
        etAmount.setText(String.valueOf(currentBudget));
        etAmount.selectAll();

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                    String input = etAmount.getText().toString().trim();
                    try {
                        double budget = Double.parseDouble(input);
                        if (budget > 0) {
                            prefManager.setMonthlyBudget(budget);
                            displayCurrentSettings();
                            if (viewModel != null) viewModel.refreshDashboardData();
                            Toast.makeText(requireContext(), "Monthly budget set to " + CurrencyHelper.formatAmount(requireContext(), budget), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Budget must be greater than zero", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showDarkModeDialog() {
        String[] options = new String[]{"System Default", "Light Mode", "Dark Mode"};
        int current = prefManager.getDarkMode();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.pref_dark_mode)
                .setSingleChoiceItems(options, current, (dialog, which) -> {
                    prefManager.setDarkMode(which);
                    if (which == PreferenceManager.MODE_LIGHT) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (which == PreferenceManager.MODE_DARK) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                    displayCurrentSettings();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void exportExpensesCsv() {
        viewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses == null || expenses.isEmpty()) {
                Toast.makeText(requireContext(), "No transactions to export", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                File file = CsvExportHelper.generateCsvFile(requireContext(), expenses);
                Intent shareIntent = CsvExportHelper.createShareCsvIntent(requireContext(), file);
                startActivity(shareIntent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Failed to export CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        viewModel.loadAllExpenses();
    }

    private void showBackupRestoreDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.pref_backup_restore)
                .setMessage("Smart Expense stores all financial records locally and securely in an offline SQLite database on your device.\n\nAll transactions can be exported at any time into CSV format for personal archiving, spreadsheet analysis, or device backup.")
                .setPositiveButton("Export Data Now", (dialog, which) -> exportExpensesCsv())
                .setNegativeButton(R.string.dialog_close, null)
                .show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.about_desc) + "\n\n" +
                        "• Architecture: Clean Architecture + MVVM\n" +
                        "• Language: 100% Java (No Kotlin)\n" +
                        "• UI: Modern Material Design 3 + XML\n" +
                        "• Storage: SQLite Database (Offline-First)\n" +
                        "• Version: 1.0.0 (API 26 - 34)\n\n" +
                        "Designed for speed, privacy, and seamless personal financial tracking.")
                .setPositiveButton(R.string.dialog_close, null)
                .show();
    }
}
