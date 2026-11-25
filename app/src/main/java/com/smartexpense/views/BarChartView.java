package com.smartexpense.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.smartexpense.R;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private final List<String> labels = new ArrayList<>();
    private final List<Double> values = new ArrayList<>();
    private double maxValue = 1.0;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);
        backgroundBarPaint.setStyle(Paint.Style.FILL);

        labelPaint.setTextAlign(Paint.Align.CENTER);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1.5f);

        // Default placeholder days
        String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            labels.add(day);
            values.add(0.0);
        }
    }

    public void setData(Map<String, Double> dailyData) {
        labels.clear();
        values.clear();
        maxValue = 10.0;

        if (dailyData == null || dailyData.isEmpty()) {
            // Default 7 days
            String[] defaultDays = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (String day : defaultDays) {
                labels.add(day);
                values.add(0.0);
            }
        } else {
            for (Map.Entry<String, Double> entry : dailyData.entrySet()) {
                labels.add(entry.getKey());
                double val = entry.getValue() != null ? entry.getValue() : 0.0;
                values.add(val);
                if (val > maxValue) {
                    maxValue = val;
                }
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        int width = getWidth();
        int height = getHeight();

        barPaint.setColor(ContextCompat.getColor(getContext(), R.color.primary));
        backgroundBarPaint.setColor(ContextCompat.getColor(getContext(), R.color.surface_variant_light));
        labelPaint.setColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        labelPaint.setTextSize(11f * density);
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.divider_light));

        float bottomPadding = 24f * density;
        float topPadding = 20f * density;
        float chartHeight = height - bottomPadding - topPadding;

        // Draw baseline
        float baselineY = height - bottomPadding;
        canvas.drawLine(10f * density, baselineY, width - (10f * density), baselineY, gridPaint);

        int count = labels.size();
        if (count == 0) return;

        float availableWidth = width - (24f * density);
        float slotWidth = availableWidth / count;
        float barWidth = Math.min(slotWidth * 0.45f, 24f * density);
        float cornerRadius = 6f * density;

        for (int i = 0; i < count; i++) {
            float slotCenterX = (12f * density) + (i * slotWidth) + (slotWidth / 2f);
            float left = slotCenterX - (barWidth / 2f);
            float right = slotCenterX + (barWidth / 2f);

            // Draw background slot bar
            barRect.set(left, topPadding, right, baselineY);
            canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, backgroundBarPaint);

            // Draw active value bar
            double val = values.get(i);
            if (val > 0) {
                float barH = (float) ((val / maxValue) * chartHeight);
                float top = baselineY - barH;
                barRect.set(left, top, right, baselineY);
                canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, barPaint);
            }

            // Draw label below baseline
            String label = labels.get(i);
            canvas.drawText(label, slotCenterX, height - (6f * density), labelPaint);
        }
    }
}
