package com.example.sensors.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * Step counter screen.
 *
 * TYPE_STEP_COUNTER reports total steps since the last device reboot.
 * The fragment subtracts the first value to compute "steps in this session".
 */
public class StepCounterFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private TextView textView;

    private float initialSteps = -1f;

    private final Handler simulationHandler = new Handler(Looper.getMainLooper());
    private int simulatedSteps = 0;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startSensor();
                        } else {
                            textView.setText("Permission refusee.");
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup parent,
            @Nullable Bundle savedInstanceState) {

        textView = new TextView(requireContext());
        textView.setTextSize(22f);
        textView.setPadding(24, 24, 24, 24);

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        return textView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (stepCounterSensor == null) {
            textView.setText("Capteur de pas indisponible. Simulation activee.");
            startSimulation();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
        ) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        } else {
            startSensor();
        }
    }

    private void startSensor() {
        sensorManager.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    @Override
    public void onPause() {
        super.onPause();

        /*
         * Important battery rule:
         * when this screen is not visible, stop listening.
         */
        sensorManager.unregisterListener(this);
        simulationHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float totalStepsSinceBoot = event.values[0];

        if (initialSteps < 0f) {
            initialSteps = totalStepsSinceBoot;
        }

        int sessionSteps = (int) (totalStepsSinceBoot - initialSteps);
        updateText((int) totalStepsSinceBoot, sessionSteps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No UI needed for accuracy in this lab.
    }

    private void startSimulation() {
        simulationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulatedSteps += 2;
                updateText(1200 + simulatedSteps, simulatedSteps);
                simulationHandler.postDelayed(this, 1200);
            }
        }, 500);
    }

    private void updateText(int total, int session) {
        textView.setText(
                "Pas depuis le dernier redemarrage : " + total
                        + "\n\nPas de la session : " + session
        );
    }
}
