package com.example.sensors.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Very small custom graph view.
 *
 * The point of the lab is sensors, so this avoids adding a charting library.
 * The view stores the latest values and draws a simple line chart with Canvas.
 */
public class LineChartView extends View {

    private final List<Float> values = new ArrayList<>();
    private final int maxPoints = 80;

    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LineChartView(Context context) {
        super(context);

        axisPaint.setColor(Color.rgb(170, 178, 186));
        axisPaint.setStrokeWidth(3f);

        gridPaint.setColor(Color.rgb(224, 229, 234));
        gridPaint.setStrokeWidth(1.5f);

        linePaint.setColor(Color.rgb(25, 118, 210));
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);

        textPaint.setColor(Color.rgb(55, 65, 81));
        textPaint.setTextSize(28f);
    }

    public void addValue(float value) {
        if (values.size() >= maxPoints) {
            values.remove(0);
        }

        values.add(value);

        /*
         * invalidate() asks Android to call onDraw() again.
         * Sensor callbacks happen often, so the chart refreshes in real time.
         */
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int left = 48;
        int top = 28;
        int right = width - 24;
        int bottom = height - 48;

        for (int i = 0; i <= 4; i++) {
            float y = top + i * ((bottom - top) / 4f);
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        canvas.drawLine(left, bottom, right, bottom, axisPaint);
        canvas.drawLine(left, top, left, bottom, axisPaint);

        if (values.size() < 2) {
            canvas.drawText("En attente des donnees...", left + 16, height / 2f, textPaint);
            return;
        }

        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;

        for (float value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        if (max == min) {
            max = min + 1f;
        }

        Path path = new Path();

        for (int i = 0; i < values.size(); i++) {
            float x = left + i * ((right - left) / (float) (maxPoints - 1));
            float normalizedValue = (values.get(i) - min) / (max - min);
            float y = bottom - normalizedValue * (bottom - top);

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        canvas.drawPath(path, linePaint);
        canvas.drawText(
                String.format(Locale.US, "Min: %.2f | Max: %.2f", min, max),
                left + 12,
                top + 30,
                textPaint
        );
    }
}
