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
import com.smartexpense.models.CategorySummary;
import com.smartexpense.utils.CurrencyHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.SummaryViewHolder> {

    private final Context context;
    private List<CategorySummary> summaries = new ArrayList<>();

    public CategorySummaryAdapter(Context context) {
        this.context = context;
    }

    public void setSummaries(List<CategorySummary> summaries) {
        this.summaries = summaries != null ? summaries : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_summary, parent, false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        CategorySummary item = summaries.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return summaries.size();
    }

    class SummaryViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout flIconBg;
        private final ImageView ivIcon;
        private final TextView tvName;
        private final TextView tvCount;
        private final TextView tvAmount;
        private final TextView tvPercentage;
        private final ProgressBar pbSummary;

        public SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            flIconBg = itemView.findViewById(R.id.fl_cat_sum_icon_bg);
            ivIcon = itemView.findViewById(R.id.iv_cat_sum_icon);
            tvName = itemView.findViewById(R.id.tv_cat_sum_name);
            tvCount = itemView.findViewById(R.id.tv_cat_sum_count);
            tvAmount = itemView.findViewById(R.id.tv_cat_sum_amount);
            tvPercentage = itemView.findViewById(R.id.tv_cat_sum_percentage);
            pbSummary = itemView.findViewById(R.id.pb_cat_sum);
        }

        public void bind(CategorySummary item) {
            tvName.setText(item.getCategory());
            tvCount.setText(item.getCount() + (item.getCount() == 1 ? " transaction" : " transactions"));
            tvAmount.setText(CurrencyHelper.formatAmount(context, item.getTotalAmount()));

            String pctText = String.format(Locale.getDefault(), "%.1f%%", item.getPercentage());
            tvPercentage.setText(pctText);
            pbSummary.setProgress((int) Math.round(item.getPercentage()));

            setupAppearance(item.getCategory());
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
