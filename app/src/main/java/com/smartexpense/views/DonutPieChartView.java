package com.smartexpense.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.smartexpense.R;
import com.smartexpense.models.CategorySummary;
import com.smartexpense.utils.CurrencyHelper;
import java.util.ArrayList;
import java.util.List;

public class DonutPieChartView extends View {

    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subtextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();

    private List<CategorySummary> summaries = new ArrayList<>();
    private double totalSpent = 0.0;
    private float strokeWidth = 36f;

    public DonutPieChartView(Context context) {
        super(context);
        init();
    }

    public DonutPieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DonutPieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        subtextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<CategorySummary> data, double total) {
        this.summaries = data != null ? data : new ArrayList<>();
        this.totalSpent = total;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float density = getResources().getDisplayMetrics().density;
        strokeWidth = 18f * density;
        arcPaint.setStrokeWidth(strokeWidth);

        float padding = strokeWidth / 2f + (8f * density);
        float size = Math.min(w, h) - (padding * 2f);
        float left = (w - size) / 2f;
        float top = (h - size) / 2f;
        bounds.set(left, top, left + size, top + size);

        textPaint.setTextSize(18f * density);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_primary));

        subtextPaint.setTextSize(12f * density);
        subtextPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        if (summaries == null || summaries.isEmpty() || totalSpent <= 0) {
            // Draw empty placeholder ring
            arcPaint.setColor(ContextCompat.getColor(getContext(), R.color.surface_variant_light));
            canvas.drawArc(bounds, 0f, 360f, false, arcPaint);

            canvas.drawText("No Expenses", centerX, centerY - (2f * density), textPaint);
            canvas.drawText("in this period", centerX, centerY + (16f * density), subtextPaint);
            return;
        }

        float startAngle = -90f;
        float spaceAngle = summaries.size() > 1 ? 2.5f : 0f;

        for (CategorySummary item : summaries) {
            float sweepAngle = (float) ((item.getTotalAmount() / totalSpent) * 360f);
            if (sweepAngle > spaceAngle) {
                sweepAngle -= spaceAngle;
            }

            arcPaint.setColor(getCategoryColor(item.getCategory()));
            canvas.drawArc(bounds, startAngle + (spaceAngle / 2f), sweepAngle, false, arcPaint);
            startAngle += (sweepAngle + spaceAngle);
        }

        // Center Text
        String formattedTotal = CurrencyHelper.formatAmount(getContext(), totalSpent);
        canvas.drawText(formattedTotal, centerX, centerY, textPaint);
        canvas.drawText("Total Spent", centerX, centerY + (18f * density), subtextPaint);
    }

    private int getCategoryColor(String category) {
        if (category == null) return ContextCompat.getColor(getContext(), R.color.cat_other);
        switch (category.toLowerCase()) {
            case "food":
                return ContextCompat.getColor(getContext(), R.color.cat_food);
            case "transport":
                return ContextCompat.getColor(getContext(), R.color.cat_transport);
            case "shopping":
                return ContextCompat.getColor(getContext(), R.color.cat_shopping);
            case "bills":
                return ContextCompat.getColor(getContext(), R.color.cat_bills);
            case "health":
                return ContextCompat.getColor(getContext(), R.color.cat_health);
            case "education":
                return ContextCompat.getColor(getContext(), R.color.cat_education);
            case "entertainment":
                return ContextCompat.getColor(getContext(), R.color.cat_entertainment);
            default:
                return ContextCompat.getColor(getContext(), R.color.cat_other);
        }
    }
}
