package com.smartexpense.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.smartexpense.R;
import com.smartexpense.adapters.CategoryBudgetAdapter;
import com.smartexpense.models.CategoryBudget;
import com.smartexpense.utils.CurrencyHelper;
import com.smartexpense.viewmodels.ExpenseViewModel;
import com.smartexpense.viewmodels.ExpenseViewModelFactory;

public class CategoryBudgetActivity extends AppCompatActivity implements CategoryBudgetAdapter.OnCategoryBudgetClickListener {

    private ExpenseViewModel viewModel;
    private CategoryBudgetAdapter adapter;
    private RecyclerView rvCategoryBudgets;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_budget);

        viewModel = new ViewModelProvider(this, new ExpenseViewModelFactory(getApplication())).get(ExpenseViewModel.class);

        toolbar = findViewById(R.id.toolbar_category_budget);
        rvCategoryBudgets = findViewById(R.id.rv_category_budgets);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        loadCategoryBudgets();
    }

    private void setupRecyclerView() {
        adapter = new CategoryBudgetAdapter(this, this);
        rvCategoryBudgets.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryBudgets.setAdapter(adapter);
    }

    private void loadCategoryBudgets() {
        viewModel.getCategoryBudgets(list -> {
            if (list != null) {
                adapter.setBudgets(list);
            }
        });
    }

    @Override
    public void onEditBudget(CategoryBudget budget) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_budget, null);
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_budget_title);
        TextView tvDesc = dialogView.findViewById(R.id.tv_dialog_budget_desc);
        EditText etAmount = dialogView.findViewById(R.id.et_dialog_budget_amount);

        tvTitle.setText(budget.getCategory() + " Budget");
        tvDesc.setText("Set the monthly spending limit for " + budget.getCategory() + ".");
        etAmount.setText(String.valueOf(budget.getBudgetAmount()));
        etAmount.selectAll();

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                    String input = etAmount.getText().toString().trim();
                    try {
                        double newLimit = Double.parseDouble(input);
                        if (newLimit > 0) {
                            viewModel.setCategoryBudget(budget.getCategory(), newLimit, unused -> {
                                Toast.makeText(this, budget.getCategory() + " budget set to " + CurrencyHelper.formatAmount(this, newLimit), Toast.LENGTH_SHORT).show();
                                loadCategoryBudgets();
                            });
                        } else {
                            Toast.makeText(this, "Budget limit must be greater than zero", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }
}
