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
 * Fragment for x/y/z motion sensors:
 * accelerometer, gravity, gyroscope.
 */
public class MotionSensorFragment extends Fragment implements SensorEventListener {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE = "title";

    private SensorManager sensorManager;
    private Sensor sensor;

    private TextView valuesView;
    private LineChartView chartView;

    private int sensorType;
    private String title;

    private final Handler simulationHandler = new Handler(Looper.getMainLooper());
    private float simulationTime = 0f;

    public static MotionSensorFragment newInstance(int sensorType, String title) {
        MotionSensorFragment fragment = new MotionSensorFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR_TYPE, sensorType);
        args.putString(ARG_TITLE, title);

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

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView titleView = new TextView(requireContext());
        titleView.setText(title);
        titleView.setTextSize(22f);

        valuesView = new TextView(requireContext());
        valuesView.setTextSize(18f);
        valuesView.setPadding(0, 24, 0, 24);

        chartView = new LineChartView(requireContext());
        chartView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                620
        ));

        layout.addView(titleView);
        layout.addView(valuesView);
        layout.addView(chartView);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sensor != null) {
            valuesView.setText("Capteur reel actif : " + sensor.getName());
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            valuesView.setText("Capteur indisponible. Simulation activee.");
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
        updateUi(event.values[0], event.values[1], event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Accuracy changes are not central to this lab.
    }

    private void updateUi(float x, float y, float z) {
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

        valuesView.setText(String.format(
                Locale.US,
                "X : %.3f\nY : %.3f\nZ : %.3f\nNorme : %.3f",
                x,
                y,
                z,
                magnitude
        ));

        chartView.addValue(magnitude);
    }

    private void startSimulation() {
        simulationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulationTime++;

                float x = (float) Math.sin(simulationTime / 3f) * 2f;
                float y = (float) Math.cos(simulationTime / 4f) * 2f;
                float z = sensorType == Sensor.TYPE_GYROSCOPE
                        ? (float) Math.sin(simulationTime / 5f)
                        : 9.81f + (float) Math.sin(simulationTime / 6f);

                updateUi(x, y, z);
                simulationHandler.postDelayed(this, 700);
            }
        }, 400);
    }
}
