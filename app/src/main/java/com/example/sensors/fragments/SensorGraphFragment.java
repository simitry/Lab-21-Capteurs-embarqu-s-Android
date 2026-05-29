package com.example.sensors.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sensors.views.LineChartView;

import java.util.Locale;

/**
 * Generic graph screen for simple sensors.
 *
 * It can show:
 * - first value only, for temperature/humidity/proximity;
 * - magnitude, for magnetic field x/y/z.
 */
public class SensorGraphFragment extends Fragment implements SensorEventListener {

    public static final String MODE_FIRST_VALUE = "FIRST_VALUE";
    public static final String MODE_MAGNITUDE = "MAGNITUDE";

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE = "title";
    private static final String ARG_MODE = "mode";

    private SensorManager sensorManager;
    private Sensor sensor;

    private TextView valueView;
    private LineChartView chartView;

    private int sensorType;
    private String title;
    private String mode;

    /*
     * Many emulators do not expose temperature/humidity/proximity.
     * Simulation keeps those screens useful for the lab instead of blank.
     */
    private final Handler simulationHandler = new Handler(Looper.getMainLooper());
    private float simulationTime = 0f;

    public static SensorGraphFragment newInstance(int sensorType, String title, String mode) {
        SensorGraphFragment fragment = new SensorGraphFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR_TYPE, sensorType);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MODE, mode);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup parent,
            @Nullable Bundle savedInstanceState) {

        sensorType = requireArguments().getInt(ARG_SENSOR_TYPE);
        title = requireArguments().getString(ARG_TITLE);
        mode = requireArguments().getString(ARG_MODE);

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView titleView = new TextView(requireContext());
        titleView.setText(title);
        titleView.setTextSize(22f);
        titleView.setPadding(0, 0, 0, 20);

        valueView = new TextView(requireContext());
        valueView.setTextSize(18f);
        valueView.setPadding(0, 0, 0, 20);

        chartView = new LineChartView(requireContext());
        chartView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                620
        ));

        layout.addView(titleView);
        layout.addView(valueView);
        layout.addView(chartView);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sensor != null) {
            valueView.setText("Capteur reel actif : " + sensor.getName());
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            valueView.setText("Capteur indisponible. Simulation activee.");
            startSimulation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
        simulationHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float value = extractValue(event.values);
        updateUi(value);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this lab screen.
    }

    private float extractValue(float[] values) {
        if (MODE_MAGNITUDE.equals(mode) && values.length >= 3) {
            return (float) Math.sqrt(
                    values[0] * values[0]
                            + values[1] * values[1]
                            + values[2] * values[2]
            );
        }

        return values[0];
    }

    private void updateUi(float value) {
        valueView.setText(String.format(Locale.US, "Valeur detectee : %.3f", value));
        chartView.addValue(value);
    }

    private void startSimulation() {
        simulationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulationTime++;
                updateUi(simulatedValue());
                simulationHandler.postDelayed(this, 1000);
            }
        }, 400);
    }

    private float simulatedValue() {
        if (sensorType == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            return 24f + (float) Math.sin(simulationTime / 5f) * 3f;
        }
        if (sensorType == Sensor.TYPE_RELATIVE_HUMIDITY) {
            return 55f + (float) Math.sin(simulationTime / 7f) * 15f;
        }
        if (sensorType == Sensor.TYPE_PROXIMITY) {
            return simulationTime % 6 < 3 ? 0f : 5f;
        }
        if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
            return 45f + (float) Math.sin(simulationTime / 4f) * 10f;
        }
        return (float) Math.sin(simulationTime);
    }
}
