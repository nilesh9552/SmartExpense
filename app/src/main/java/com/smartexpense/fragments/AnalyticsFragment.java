package com.smartexpense.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.smartexpense.R;
import com.smartexpense.adapters.CategorySummaryAdapter;
import com.smartexpense.models.CategorySummary;
import com.smartexpense.models.MonthlyComparison;
import com.smartexpense.utils.CurrencyHelper;
import com.smartexpense.utils.DateUtils;
import com.smartexpense.viewmodels.ExpenseViewModel;
import com.smartexpense.viewmodels.ExpenseViewModelFactory;
import com.smartexpense.views.BarChartView;
import com.smartexpense.views.DonutPieChartView;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private ExpenseViewModel viewModel;
    private CategorySummaryAdapter categorySummaryAdapter;

    private ChipGroup chipGroupTimeframe;
    private TextView tvTotalSpend;
    private TextView tvDailyAvg;
    private TextView tvHighestCat;
    private TextView tvTxCount;
    private ImageView ivComparisonTrend;
    private TextView tvComparisonText;
    private BarChartView barChartView;
    private DonutPieChartView donutChartView;
    private RecyclerView rvCategories;

    private long startDate = 0;
    private long endDate = 0;
    private int daysInPeriod = 30;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity(), new ExpenseViewModelFactory(requireActivity().getApplication())).get(ExpenseViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupTimeframeSelector();

        // Default to This Month
        selectTimeframe(R.id.chip_analytics_month);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAnalyticsData();
    }

    private void initViews(View view) {
        chipGroupTimeframe = view.findViewById(R.id.chip_group_analytics_timeframe);
        tvTotalSpend = view.findViewById(R.id.tv_stat_total_spend);
        tvDailyAvg = view.findViewById(R.id.tv_stat_daily_avg);
        tvHighestCat = view.findViewById(R.id.tv_stat_highest_cat);
        tvTxCount = view.findViewById(R.id.tv_stat_tx_count);
        ivComparisonTrend = view.findViewById(R.id.iv_comparison_trend);
        tvComparisonText = view.findViewById(R.id.tv_comparison_text);
        barChartView = view.findViewById(R.id.view_bar_chart);
        donutChartView = view.findViewById(R.id.view_donut_chart);
        rvCategories = view.findViewById(R.id.rv_analytics_categories);
    }

    private void setupRecyclerView() {
        categorySummaryAdapter = new CategorySummaryAdapter(requireContext());
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategories.setAdapter(categorySummaryAdapter);
    }

    private void setupTimeframeSelector() {
        chipGroupTimeframe.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            selectTimeframe(checkedIds.get(0));
        });
    }

    private void selectTimeframe(int chipId) {
        long now = System.currentTimeMillis();

        if (chipId == R.id.chip_analytics_week) {
            startDate = DateUtils.getStartOfWeek(now);
            endDate = DateUtils.getEndOfWeek(now);
            daysInPeriod = 7;
        } else if (chipId == R.id.chip_analytics_year) {
            startDate = DateUtils.getStartOfYear(now);
            endDate = DateUtils.getEndOfYear(now);
            daysInPeriod = 365;
        } else if (chipId == R.id.chip_analytics_all) {
            startDate = 0;
            endDate = Long.MAX_VALUE;
            daysInPeriod = 90;
        } else { // Month default
            startDate = DateUtils.getStartOfMonth(now);
            endDate = DateUtils.getEndOfMonth(now);
            daysInPeriod = 30;
        }

        loadAnalyticsData();
    }

    private void loadAnalyticsData() {
        if (viewModel == null) return;

        viewModel.getAnalyticsData(startDate, endDate, result -> {
            if (result == null || !isAdded()) return;

            // Total spent
            tvTotalSpend.setText(CurrencyHelper.formatAmount(requireContext(), result.totalSpent));

            // Daily average
            double dailyAvg = daysInPeriod > 0 ? (result.totalSpent / daysInPeriod) : 0.0;
            tvDailyAvg.setText(CurrencyHelper.formatAmount(requireContext(), dailyAvg));

            // Total transactions
            int count = result.expenses != null ? result.expenses.size() : 0;
            tvTxCount.setText(String.valueOf(count));

            // Top category
            if (result.summaries != null && !result.summaries.isEmpty()) {
                CategorySummary top = result.summaries.get(0);
                tvHighestCat.setText(top.getCategory() + " (" + CurrencyHelper.formatAmount(requireContext(), top.getTotalAmount()) + ")");
            } else {
                tvHighestCat.setText("None");
            }

            // Charts
            donutChartView.setData(result.summaries, result.totalSpent);
            barChartView.setData(result.dailySpending);

            // Category breakdown list
            categorySummaryAdapter.setSummaries(result.summaries);

            // Monthly comparison
            updateMonthlyComparison(result.monthlyComparison);
        });
    }

    private void updateMonthlyComparison(MonthlyComparison comparison) {
        if (comparison == null) return;

        if (comparison.getPreviousMonthTotal() == 0 && comparison.getCurrentMonthTotal() == 0) {
            tvComparisonText.setText(R.string.comparison_same);
            ivComparisonTrend.setImageResource(R.drawable.ic_check);
            ivComparisonTrend.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary));
            return;
        }

        String diffAmount = CurrencyHelper.formatAmount(requireContext(), comparison.getDifference());
        double pct = comparison.getPercentageChange();

        if (comparison.isIncrease()) {
            tvComparisonText.setText(getString(R.string.comparison_more, diffAmount, pct));
            ivComparisonTrend.setImageResource(R.drawable.ic_trending_up);
            ivComparisonTrend.setColorFilter(ContextCompat.getColor(requireContext(), R.color.status_warning));
        } else {
            tvComparisonText.setText(getString(R.string.comparison_less, diffAmount, Math.abs(pct)));
            ivComparisonTrend.setImageResource(R.drawable.ic_trending_down);
            ivComparisonTrend.setColorFilter(ContextCompat.getColor(requireContext(), R.color.status_good));
        }
    }
}
