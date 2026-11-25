package com.smartexpense.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartexpense.R;
import com.smartexpense.activities.AddEditExpenseActivity;
import com.smartexpense.adapters.ExpenseAdapter;
import com.smartexpense.models.Expense;
import com.smartexpense.utils.CurrencyHelper;
import com.smartexpense.utils.DateUtils;
import com.smartexpense.viewmodels.ExpenseViewModel;
import com.smartexpense.viewmodels.ExpenseViewModelFactory;
import java.util.Calendar;
import java.util.List;

public class HistoryFragment extends Fragment implements ExpenseAdapter.OnExpenseClickListener {

    private ExpenseViewModel viewModel;
    private ExpenseAdapter expenseAdapter;

    private EditText etSearch;
    private ImageView btnClearSearch;
    private ChipGroup chipGroupCategories;
    private ChipGroup chipGroupDates;
    private TextView tvFilteredTotal;
    private TextView tvFilteredCount;
    private RecyclerView rvHistoryExpenses;
    private LinearLayout layoutEmptyHistory;

    private String currentSearch = "";
    private String selectedCategory = "All";
    private long startDate = 0;
    private long endDate = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity(), new ExpenseViewModelFactory(requireActivity().getApplication())).get(ExpenseViewModel.class);

        initViews(view);
        setupSearch();
        setupFilters();
        setupRecyclerView();
        observeViewModel();

        applyFilter();
    }

    @Override
    public void onResume() {
        super.onResume();
        applyFilter();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search_expenses);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);
        chipGroupCategories = view.findViewById(R.id.chip_group_categories);
        chipGroupDates = view.findViewById(R.id.chip_group_dates);
        tvFilteredTotal = view.findViewById(R.id.tv_filtered_total);
        tvFilteredCount = view.findViewById(R.id.tv_filtered_count);
        rvHistoryExpenses = view.findViewById(R.id.rv_history_expenses);
        layoutEmptyHistory = view.findViewById(R.id.layout_empty_history);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                btnClearSearch.setVisibility(currentSearch.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
        });
    }

    private void setupFilters() {
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategory = "All";
            } else {
                int id = checkedIds.get(0);
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    selectedCategory = chip.getText().toString();
                }
            }
            applyFilter();
        });

        chipGroupDates.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                startDate = 0;
                endDate = 0;
                applyFilter();
                return;
            }

            int id = checkedIds.get(0);
            long now = System.currentTimeMillis();

            if (id == R.id.chip_date_today) {
                startDate = DateUtils.getStartOfDay(now);
                endDate = DateUtils.getEndOfDay(now);
                applyFilter();
            } else if (id == R.id.chip_date_week) {
                startDate = DateUtils.getStartOfWeek(now);
                endDate = DateUtils.getEndOfWeek(now);
                applyFilter();
            } else if (id == R.id.chip_date_month) {
                startDate = DateUtils.getStartOfMonth(now);
                endDate = DateUtils.getEndOfMonth(now);
                applyFilter();
            } else if (id == R.id.chip_date_custom) {
                showDateRangePicker();
            } else { // chip_date_all
                startDate = 0;
                endDate = 0;
                applyFilter();
            }
        });
    }

    private void showDateRangePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog startPicker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar startCal = Calendar.getInstance();
            startCal.set(year, month, dayOfMonth);
            long chosenStart = DateUtils.getStartOfDay(startCal.getTimeInMillis());

            // Open end date picker
            DatePickerDialog endPicker = new DatePickerDialog(requireContext(), (v2, y2, m2, d2) -> {
                Calendar endCal = Calendar.getInstance();
                endCal.set(y2, m2, d2);
                long chosenEnd = DateUtils.getEndOfDay(endCal.getTimeInMillis());

                if (chosenEnd >= chosenStart) {
                    startDate = chosenStart;
                    endDate = chosenEnd;
                } else {
                    startDate = chosenEnd;
                    endDate = chosenStart;
                }

                Chip chipCustom = chipGroupDates.findViewById(R.id.chip_date_custom);
                if (chipCustom != null) {
                    chipCustom.setText(DateUtils.formatSimpleDate(startDate) + " - " + DateUtils.formatSimpleDate(endDate));
                }
                applyFilter();
            }, year, month, dayOfMonth);

            endPicker.setTitle("Select End Date");
            endPicker.show();

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        startPicker.setTitle("Select Start Date");
        startPicker.show();
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter(requireContext(), this);
        rvHistoryExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistoryExpenses.setAdapter(expenseAdapter);
    }

    private void observeViewModel() {
        viewModel.getFilteredExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                expenseAdapter.setExpenses(expenses);
                rvHistoryExpenses.setVisibility(View.VISIBLE);
                layoutEmptyHistory.setVisibility(View.GONE);

                double total = 0.0;
                for (Expense e : expenses) {
                    total += e.getAmount();
                }

                tvFilteredTotal.setText("Total: " + CurrencyHelper.formatAmount(requireContext(), total));
                tvFilteredCount.setText(expenses.size() + (expenses.size() == 1 ? " transaction" : " transactions"));
            } else {
                expenseAdapter.setExpenses(null);
                rvHistoryExpenses.setVisibility(View.GONE);
                layoutEmptyHistory.setVisibility(View.VISIBLE);

                tvFilteredTotal.setText("Total: " + CurrencyHelper.formatAmount(requireContext(), 0.0));
                tvFilteredCount.setText("0 transactions");
            }
        });
    }

    private void applyFilter() {
        if (viewModel != null) {
            viewModel.filterExpenses(currentSearch, selectedCategory, startDate, endDate);
        }
    }

    @Override
    public void onExpenseClick(Expense expense) {
        onExpenseEdit(expense);
    }

    @Override
    public void onExpenseEdit(Expense expense) {
        Intent intent = new Intent(requireContext(), AddEditExpenseActivity.class);
        intent.putExtra(AddEditExpenseActivity.EXTRA_EXPENSE_ID, expense.getId());
        startActivity(intent);
    }

    @Override
    public void onExpenseDelete(Expense expense) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, expense.getName()))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    viewModel.deleteExpense(expense.getId(), rows -> {
                        applyFilter();
                        viewModel.refreshDashboardData();
                    });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }
}
