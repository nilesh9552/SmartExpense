package com.smartexpense.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.smartexpense.R;
import com.smartexpense.models.Expense;
import com.smartexpense.utils.CurrencyHelper;
import com.smartexpense.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
        void onExpenseEdit(Expense expense);
        void onExpenseDelete(Expense expense);
    }

    private final Context context;
    private List<Expense> expenses = new ArrayList<>();
    private final OnExpenseClickListener listener;

    public ExpenseAdapter(Context context, OnExpenseClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout flIconBg;
        private final ImageView ivCategoryIcon;
        private final TextView tvTitle;
        private final TextView tvCategoryBadge;
        private final TextView tvPaymentBadge;
        private final TextView tvDate;
        private final TextView tvAmount;
        private final ImageView ivMenu;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            flIconBg = itemView.findViewById(R.id.fl_category_icon_bg);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvTitle = itemView.findViewById(R.id.tv_expense_title);
            tvCategoryBadge = itemView.findViewById(R.id.tv_category_badge);
            tvPaymentBadge = itemView.findViewById(R.id.tv_payment_badge);
            tvDate = itemView.findViewById(R.id.tv_expense_date);
            tvAmount = itemView.findViewById(R.id.tv_expense_amount);
            ivMenu = itemView.findViewById(R.id.iv_expense_menu);
        }

        public void bind(Expense expense) {
            tvTitle.setText(expense.getName());
            tvCategoryBadge.setText(expense.getCategory());
            tvPaymentBadge.setText(expense.getPaymentMethod());
            tvDate.setText(DateUtils.formatDate(expense.getDate()));

            String formattedAmount = "- " + CurrencyHelper.formatAmount(context, expense.getAmount());
            tvAmount.setText(formattedAmount);

            setupCategoryAppearance(expense.getCategory());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onExpenseClick(expense);
            });

            ivMenu.setOnClickListener(v -> showPopupMenu(v, expense));
        }

        private void showPopupMenu(View view, Expense expense) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.getMenu().add(0, 1, 0, R.string.action_edit);
            popup.getMenu().add(0, 2, 1, R.string.action_delete);

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    if (listener != null) listener.onExpenseEdit(expense);
                    return true;
                } else if (item.getItemId() == 2) {
                    if (listener != null) listener.onExpenseDelete(expense);
                    return true;
                }
                return false;
            });
            popup.show();
        }

        private void setupCategoryAppearance(String category) {
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
            ivCategoryIcon.setImageResource(iconRes);
            ivCategoryIcon.setColorFilter(color);
            tvCategoryBadge.setTextColor(color);
        }
    }
}
