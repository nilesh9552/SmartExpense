package com.smartexpense.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.smartexpense.R;
import com.smartexpense.models.CategoryBudget;
import com.smartexpense.utils.CurrencyHelper;
import java.util.ArrayList;
import java.util.List;

public class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.BudgetViewHolder> {

    public interface OnCategoryBudgetClickListener {
        void onEditBudget(CategoryBudget budget);
    }

    private final Context context;
    private List<CategoryBudget> budgets = new ArrayList<>();
    private final OnCategoryBudgetClickListener listener;

    public CategoryBudgetAdapter(Context context, OnCategoryBudgetClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setBudgets(List<CategoryBudget> budgets) {
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        CategoryBudget item = budgets.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout flIconBg;
        private final ImageView ivIcon;
        private final TextView tvName;
        private final TextView tvSpentLimit;
        private final ImageView btnEdit;
        private final ProgressBar pbBudget;
        private final TextView tvPercent;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            flIconBg = itemView.findViewById(R.id.fl_cat_budget_icon_bg);
            ivIcon = itemView.findViewById(R.id.iv_cat_budget_icon);
            tvName = itemView.findViewById(R.id.tv_cat_budget_name);
            tvSpentLimit = itemView.findViewById(R.id.tv_cat_budget_spent_limit);
            btnEdit = itemView.findViewById(R.id.btn_edit_cat_budget);
            pbBudget = itemView.findViewById(R.id.pb_cat_budget);
            tvPercent = itemView.findViewById(R.id.tv_cat_budget_percent);
        }

        public void bind(CategoryBudget item) {
            tvName.setText(item.getCategory());

            String spentLimitText = "Spent: " + CurrencyHelper.formatAmount(context, item.getSpentAmount()) +
                    " / Limit: " + CurrencyHelper.formatAmount(context, item.getBudgetAmount());
            tvSpentLimit.setText(spentLimitText);

            int pct = item.getPercentageUsed();
            tvPercent.setText(pct + "%");
            pbBudget.setProgress(Math.min(pct, 100));

            if (pct >= 100) {
                tvPercent.setTextColor(ContextCompat.getColor(context, R.color.status_danger));
            } else if (pct >= 80) {
                tvPercent.setTextColor(ContextCompat.getColor(context, R.color.status_warning));
            } else {
                tvPercent.setTextColor(ContextCompat.getColor(context, R.color.primary));
            }

            setupAppearance(item.getCategory());

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditBudget(item);
            });
        }

        private void setupAppearance(String category) {
            int iconRes = R.drawable.ic_other;
            int colorRes = R.color.cat_other;

            if (category != null) {
                switch (category.toLowerCase()) {
                    case "food":
                        iconRes = R.drawable.ic_food;
                        colorRes = R.color.cat_food;
                        break;
                    case "transport":
                        iconRes = R.drawable.ic_transport;
                        colorRes = R.color.cat_transport;
                        break;
                    case "shopping":
                        iconRes = R.drawable.ic_shopping;
                        colorRes = R.color.cat_shopping;
                        break;
                    case "bills":
                        iconRes = R.drawable.ic_bills;
                        colorRes = R.color.cat_bills;
                        break;
                    case "health":
                        iconRes = R.drawable.ic_health;
                        colorRes = R.color.cat_health;
                        break;
                    case "education":
                        iconRes = R.drawable.ic_education;
                        colorRes = R.color.cat_education;
                        break;
                    case "entertainment":
                        iconRes = R.drawable.ic_entertainment;
                        colorRes = R.color.cat_entertainment;
                        break;
                }
            }

            int color = ContextCompat.getColor(context, colorRes);
            ivIcon.setImageResource(iconRes);
            ivIcon.setColorFilter(color);
        }
    }
}
