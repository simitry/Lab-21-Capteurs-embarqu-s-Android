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

import java.util.Locale;

/**
 * Compass built from accelerometer + magnetometer.
 */
public class CompassFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private TextView textView;

    private final float[] gravityValues = new float[3];
    private final float[] magneticValues = new float[3];

    private boolean hasGravity = false;
    private boolean hasMagnetic = false;

    private final Handler simulationHandler = new Handler(Looper.getMainLooper());
    private float simulatedDegree = 0f;

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
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return textView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        } else {
            textView.setText("Boussole indisponible : capteur manquant. Simulation activee.");
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, 3);
            hasGravity = true;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticValues, 0, 3);
            hasMagnetic = true;
        }

        if (hasGravity && hasMagnetic) {
            updateCompassFromSensors();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Could show calibration hints here in a bigger app.
    }

    private void updateCompassFromSensors() {
        float[] rotationMatrix = new float[9];
        float[] orientation = new float[3];

        boolean success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                gravityValues,
                magneticValues
        );

        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientation);

            float azimuthDegrees = (float) Math.toDegrees(orientation[0]);
            if (azimuthDegrees < 0f) {
                azimuthDegrees += 360f;
            }

            updateText(azimuthDegrees);
        }
    }

    private void startSimulation() {
        simulationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulatedDegree = (simulatedDegree + 12f) % 360f;
                updateText(simulatedDegree);
                simulationHandler.postDelayed(this, 700);
            }
        }, 400);
    }

    private void updateText(float degree) {
        textView.setText(String.format(
                Locale.US,
                "Direction : %.1f deg\n%s",
                degree,
                getDirectionName(degree)
        ));
    }

    private String getDirectionName(float degree) {
        if (degree >= 337.5f || degree < 22.5f) {
            return "Nord";
        } else if (degree < 67.5f) {
            return "Nord-Est";
        } else if (degree < 112.5f) {
            return "Est";
        } else if (degree < 157.5f) {
            return "Sud-Est";
        } else if (degree < 202.5f) {
            return "Sud";
        } else if (degree < 247.5f) {
            return "Sud-Ouest";
        } else if (degree < 292.5f) {
            return "Ouest";
        } else {
            return "Nord-Ouest";
        }
    }
}
