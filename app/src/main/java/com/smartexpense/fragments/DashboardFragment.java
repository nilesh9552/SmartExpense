package com.smartexpense.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartexpense.R;
import com.smartexpense.activities.AddEditExpenseActivity;
import com.smartexpense.activities.MainActivity;
import com.smartexpense.adapters.CategorySummaryAdapter;
import com.smartexpense.adapters.ExpenseAdapter;
import com.smartexpense.models.Expense;
import com.smartexpense.utils.CurrencyHelper;
import com.smartexpense.utils.DateUtils;
import com.smartexpense.utils.PreferenceManager;
import com.smartexpense.viewmodels.ExpenseViewModel;
import com.smartexpense.viewmodels.ExpenseViewModelFactory;

public class DashboardFragment extends Fragment implements ExpenseAdapter.OnExpenseClickListener {

    private ExpenseViewModel viewModel;
    private PreferenceManager prefManager;

    private TextView tvCurrentMonthBadge;
    private TextView tvTotalMonthlySpent;
    private TextView tvBudgetLabel;
    private TextView tvBudgetPercent;
    private ProgressBar pbMonthlyBudget;
    private TextView tvRemainingBudget;
    private TextView tvBudgetStatusPill;
    private TextView tvTodaySpent;
    private MaterialCardView cardQuickAdd;
    private TextView btnViewAll;
    private TextView tvEmptyCategories;
    private LinearLayout layoutEmptyRecent;

    private RecyclerView rvCategorySummary;
    private RecyclerView rvRecentExpenses;

    private CategorySummaryAdapter categorySummaryAdapter;
    private ExpenseAdapter expenseAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefManager = new PreferenceManager(requireContext());
        viewModel = new ViewModelProvider(requireActivity(), new ExpenseViewModelFactory(requireActivity().getApplication())).get(ExpenseViewModel.class);

        initViews(view);
        setupRecyclerViews();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshDashboardData();
        }
    }

    private void initViews(View view) {
        tvCurrentMonthBadge = view.findViewById(R.id.tv_current_month_badge);
        tvTotalMonthlySpent = view.findViewById(R.id.tv_total_monthly_spent);
        tvBudgetLabel = view.findViewById(R.id.tv_budget_label);
        tvBudgetPercent = view.findViewById(R.id.tv_budget_percent);
        pbMonthlyBudget = view.findViewById(R.id.pb_monthly_budget);
        tvRemainingBudget = view.findViewById(R.id.tv_remaining_budget);
        tvBudgetStatusPill = view.findViewById(R.id.tv_budget_status_pill);
        tvTodaySpent = view.findViewById(R.id.tv_today_spent);
        cardQuickAdd = view.findViewById(R.id.card_quick_add);
        btnViewAll = view.findViewById(R.id.btn_view_all_history);
        tvEmptyCategories = view.findViewById(R.id.tv_empty_categories);
        layoutEmptyRecent = view.findViewById(R.id.layout_empty_recent);
        rvCategorySummary = view.findViewById(R.id.rv_category_summary);
        rvRecentExpenses = view.findViewById(R.id.rv_recent_expenses);

        tvCurrentMonthBadge.setText(DateUtils.formatMonthYear(System.currentTimeMillis()));

        cardQuickAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditExpenseActivity.class);
            startActivity(intent);
        });

        btnViewAll.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_history);
            }
        });
    }

    private void setupRecyclerViews() {
        categorySummaryAdapter = new CategorySummaryAdapter(requireContext());
        rvCategorySummary.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategorySummary.setAdapter(categorySummaryAdapter);

        expenseAdapter = new ExpenseAdapter(requireContext(), this);
        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentExpenses.setAdapter(expenseAdapter);
    }

    private void observeViewModel() {
        viewModel.getTodaySpending().observe(getViewLifecycleOwner(), todaySpent -> {
            if (todaySpent != null) {
                tvTodaySpent.setText(CurrencyHelper.formatAmount(requireContext(), todaySpent));
            }
        });

        viewModel.getMonthlySpending().observe(getViewLifecycleOwner(), monthSpent -> {
            if (monthSpent == null) monthSpent = 0.0;
            tvTotalMonthlySpent.setText(CurrencyHelper.formatAmount(requireContext(), monthSpent));

            double budget = prefManager.getMonthlyBudget();
            tvBudgetLabel.setText("Budget: " + CurrencyHelper.formatAmount(requireContext(), budget));

            double remaining = budget - monthSpent;
            if (remaining >= 0) {
                tvRemainingBudget.setText(CurrencyHelper.formatAmount(requireContext(), remaining));
                tvRemainingBudget.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_good));
            } else {
                tvRemainingBudget.setText("- " + CurrencyHelper.formatAmount(requireContext(), Math.abs(remaining)));
                tvRemainingBudget.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_danger));
            }

            int percent = budget > 0 ? (int) Math.round((monthSpent / budget) * 100) : 0;
            pbMonthlyBudget.setProgress(Math.min(percent, 100));
            tvBudgetPercent.setText(percent + "% used");

            if (percent >= 100) {
                tvBudgetStatusPill.setText("Over Budget");
                tvBudgetStatusPill.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_danger));
                tvBudgetPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_danger));
            } else if (percent >= 80) {
                tvBudgetStatusPill.setText("Approaching");
                tvBudgetStatusPill.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_warning));
                tvBudgetPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_warning));
            } else {
                tvBudgetStatusPill.setText("On Track");
                tvBudgetStatusPill.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_good));
                tvBudgetPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            }
        });

        viewModel.getMonthlyCategories().observe(getViewLifecycleOwner(), summaries -> {
            if (summaries != null && !summaries.isEmpty()) {
                categorySummaryAdapter.setSummaries(summaries);
                rvCategorySummary.setVisibility(View.VISIBLE);
                tvEmptyCategories.setVisibility(View.GONE);
            } else {
                rvCategorySummary.setVisibility(View.GONE);
                tvEmptyCategories.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getRecentExpenses().observe(getViewLifecycleOwner(), recent -> {
            if (recent != null && !recent.isEmpty()) {
                expenseAdapter.setExpenses(recent);
                rvRecentExpenses.setVisibility(View.VISIBLE);
                layoutEmptyRecent.setVisibility(View.GONE);
            } else {
                rvRecentExpenses.setVisibility(View.GONE);
                layoutEmptyRecent.setVisibility(View.VISIBLE);
            }
        });
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
                        viewModel.refreshDashboardData();
                    });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }
}
