package com.smartexpense.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.smartexpense.R;
import com.smartexpense.models.Expense;
import com.smartexpense.utils.DateUtils;
import com.smartexpense.utils.NotificationUtils;
import com.smartexpense.utils.PreferenceManager;
import com.smartexpense.viewmodels.ExpenseViewModel;
import com.smartexpense.viewmodels.ExpenseViewModelFactory;
import java.util.Calendar;

public class AddEditExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_EXPENSE_ID = "extra_expense_id";

    private ExpenseViewModel viewModel;
    private PreferenceManager prefManager;

    private MaterialToolbar toolbar;
    private TextView tvCurrencySymbolPrefix;
    private EditText etAmount;
    private TextInputLayout tilExpenseName;
    private TextInputEditText etExpenseName;
    private MaterialCardView cardSelectDate;
    private TextView tvSelectedDate;
    private ChipGroup chipGroupCategory;
    private ChipGroup chipGroupPayment;
    private TextInputLayout tilExpenseNotes;
    private TextInputEditText etExpenseNotes;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private MaterialButton btnDelete;

    private long selectedDateMillis;
    private long editingExpenseId = -1;
    private Expense currentExpense = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        prefManager = new PreferenceManager(this);
        viewModel = new ViewModelProvider(this, new ExpenseViewModelFactory(getApplication())).get(ExpenseViewModel.class);

        selectedDateMillis = System.currentTimeMillis();

        initViews();
        setupToolbar();
        setupDatePicker();
        checkEditMode();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_add_expense);
        tvCurrencySymbolPrefix = findViewById(R.id.tv_currency_symbol_prefix);
        etAmount = findViewById(R.id.et_expense_amount);
        tilExpenseName = findViewById(R.id.til_expense_name);
        etExpenseName = findViewById(R.id.et_expense_name);
        cardSelectDate = findViewById(R.id.card_select_date);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        chipGroupCategory = findViewById(R.id.chip_group_expense_category);
        chipGroupPayment = findViewById(R.id.chip_group_expense_payment);
        tilExpenseNotes = findViewById(R.id.til_expense_notes);
        etExpenseNotes = findViewById(R.id.et_expense_notes);
        btnSave = findViewById(R.id.btn_save_expense);
        btnCancel = findViewById(R.id.btn_cancel_expense);
        btnDelete = findViewById(R.id.btn_delete_expense);

        tvCurrencySymbolPrefix.setText(prefManager.getCurrencySymbol());
        tvSelectedDate.setText(DateUtils.formatDate(selectedDateMillis));

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDatePicker() {
        cardSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selectedDateMillis);

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, dayOfMonth);
                        selectedDateMillis = chosen.getTimeInMillis();
                        tvSelectedDate.setText(DateUtils.formatDate(selectedDateMillis));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_EXPENSE_ID)) {
            editingExpenseId = intent.getLongExtra(EXTRA_EXPENSE_ID, -1);
            if (editingExpenseId != -1) {
                toolbar.setTitle(R.string.edit_expense_title);
                btnSave.setText(R.string.action_update);
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> confirmDelete());

                viewModel.getExpenseById(editingExpenseId, expense -> {
                    if (expense != null) {
                        currentExpense = expense;
                        populateExpenseData(expense);
                    }
                });
            }
        }
    }

    private void populateExpenseData(Expense expense) {
        etExpenseName.setText(expense.getName());
        etAmount.setText(String.valueOf(expense.getAmount()));
        selectedDateMillis = expense.getDate();
        tvSelectedDate.setText(DateUtils.formatDate(selectedDateMillis));
        if (expense.getNotes() != null) {
            etExpenseNotes.setText(expense.getNotes());
        }

        // Select Category Chip
        selectCategoryChip(expense.getCategory());

        // Select Payment Chip
        selectPaymentChip(expense.getPaymentMethod());
    }

    private void selectCategoryChip(String category) {
        if (category == null) return;
        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            View child = chipGroupCategory.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().equalsIgnoreCase(category)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    private void selectPaymentChip(String paymentMethod) {
        if (paymentMethod == null) return;
        for (int i = 0; i < chipGroupPayment.getChildCount(); i++) {
            View child = chipGroupPayment.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().equalsIgnoreCase(paymentMethod)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    private void validateAndSave() {
        String name = etExpenseName.getText() != null ? etExpenseName.getText().toString().trim() : "";
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String notes = etExpenseNotes.getText() != null ? etExpenseNotes.getText().toString().trim() : "";

        // Validate Name
        if (name.isEmpty()) {
            tilExpenseName.setError(getString(R.string.error_name_empty));
            etExpenseName.requestFocus();
            return;
        } else {
            tilExpenseName.setError(null);
        }

        // Validate Amount
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError(getString(R.string.error_amount_invalid));
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError(getString(R.string.error_amount_invalid));
            etAmount.requestFocus();
            return;
        }

        // Get Category
        int catChipId = chipGroupCategory.getCheckedChipId();
        if (catChipId == View.NO_ID) {
            Toast.makeText(this, R.string.error_category_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        Chip catChip = findViewById(catChipId);
        String category = catChip != null ? catChip.getText().toString() : "Other";

        // Get Payment Method
        int payChipId = chipGroupPayment.getCheckedChipId();
        if (payChipId == View.NO_ID) {
            Toast.makeText(this, R.string.error_payment_method_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        Chip payChip = findViewById(payChipId);
        String paymentMethod = payChip != null ? payChip.getText().toString() : "Cash";

        if (editingExpenseId != -1 && currentExpense != null) {
            currentExpense.setName(name);
            currentExpense.setAmount(amount);
            currentExpense.setCategory(category);
            currentExpense.setPaymentMethod(paymentMethod);
            currentExpense.setDate(selectedDateMillis);
            currentExpense.setNotes(notes);

            viewModel.updateExpense(currentExpense, rows -> {
                Toast.makeText(this, R.string.msg_expense_updated, Toast.LENGTH_SHORT).show();
                checkBudgetNotification();
                finish();
            });
        } else {
            Expense newExpense = new Expense(name, amount, category, paymentMethod, selectedDateMillis, notes);
            viewModel.insertExpense(newExpense, id -> {
                Toast.makeText(this, R.string.msg_expense_saved, Toast.LENGTH_SHORT).show();
                checkBudgetNotification();
                finish();
            });
        }
    }

    private void checkBudgetNotification() {
        viewModel.getMonthlySpending().observe(this, totalSpent -> {
            if (totalSpent != null) {
                double budget = prefManager.getMonthlyBudget();
                NotificationUtils.checkAndNotifyBudget(this, totalSpent, budget);
            }
        });
    }

    private void confirmDelete() {
        if (currentExpense == null) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, currentExpense.getName()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    viewModel.deleteExpense(editingExpenseId, rows -> {
                        Toast.makeText(this, R.string.msg_expense_deleted, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }
}
