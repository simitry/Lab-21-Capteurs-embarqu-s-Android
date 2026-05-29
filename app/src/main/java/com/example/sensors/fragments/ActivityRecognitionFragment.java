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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

/**
 * Simple activity recognition from accelerometer values.
 *
 * This is not machine learning. It is a pedagogical threshold-based classifier:
 * - strong spike -> jump;
 * - regular variation -> walking;
 * - low variation -> stable posture.
 */
public class ActivityRecognitionFragment extends Fragment implements SensorEventListener {

    private static final int WINDOW_SIZE = 30;
    private static final float ALPHA = 0.8f;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView resultView;

    private final float[] gravity = new float[3];
    private final Queue<Float> movementWindow = new LinkedList<>();

    private final Handler simulationHandler = new Handler(Looper.getMainLooper());
    private float simulationTime = 0f;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup parent,
            @Nullable Bundle savedInstanceState) {

        resultView = new TextView(requireContext());
        resultView.setTextSize(22f);
        resultView.setPadding(24, 24, 24, 24);

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        return resultView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        } else {
            resultView.setText("Accelerometre indisponible. Simulation activee.");
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
        handleAcceleration(event.values[0], event.values[1], event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this simple classifier.
    }

    private void handleAcceleration(float x, float y, float z) {
        /*
         * Low-pass filter:
         * estimate gravity slowly, then subtract it to isolate movement.
         */
        gravity[0] = ALPHA * gravity[0] + (1f - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1f - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1f - ALPHA) * z;

        float linearX = x - gravity[0];
        float linearY = y - gravity[1];
        float linearZ = z - gravity[2];

        float movement = (float) Math.sqrt(
                linearX * linearX + linearY * linearY + linearZ * linearZ
        );

        addMovementValue(movement);

        String activity = classifyActivity(x, y, z);

        resultView.setText(String.format(
                Locale.US,
                "X : %.3f\nY : %.3f\nZ : %.3f\n\nMouvement : %.3f\n\nActivite detectee : %s",
                x,
                y,
                z,
                movement,
                activity
        ));
    }

    private void addMovementValue(float movement) {
        if (movementWindow.size() >= WINDOW_SIZE) {
            movementWindow.poll();
        }
        movementWindow.add(movement);
    }

    private String classifyActivity(float x, float y, float z) {
        if (movementWindow.size() < WINDOW_SIZE) {
            return "Calibration...";
        }

        float mean = 0f;
        float max = 0f;

        for (float value : movementWindow) {
            mean += value;
            max = Math.max(max, value);
        }

        mean = mean / movementWindow.size();

        float variance = 0f;
        for (float value : movementWindow) {
            variance += (value - mean) * (value - mean);
        }

        float standardDeviation = (float) Math.sqrt(variance / movementWindow.size());

        if (max > 10f) {
            return "Saut";
        }
        if (standardDeviation > 1.2f) {
            return "Marche";
        }
        if (Math.abs(z) > 8f) {
            return "Stable / telephone a plat";
        }
        if (Math.abs(y) > 7f || Math.abs(x) > 7f) {
            return "Assis ou debout selon orientation";
        }

        return "Position stable";
    }

    private void startSimulation() {
        simulationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulationTime++;

                float x = (float) Math.sin(simulationTime / 3f) * 0.8f;
                float y = (float) Math.cos(simulationTime / 4f) * 0.8f;
                float z = 9.81f;

                if (simulationTime % 35 > 15 && simulationTime % 35 < 25) {
                    x *= 4f;
                    y *= 4f;
                }
                if (simulationTime % 60 == 0) {
                    z += 12f;
                }

                handleAcceleration(x, y, z);
                simulationHandler.postDelayed(this, 180);
            }
        }, 300);
    }
}
