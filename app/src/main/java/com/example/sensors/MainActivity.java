package com.example.sensors;

import android.hardware.Sensor;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.sensors.fragments.ActivityRecognitionFragment;
import com.example.sensors.fragments.CompassFragment;
import com.example.sensors.fragments.MotionSensorFragment;
import com.example.sensors.fragments.SensorGraphFragment;
import com.example.sensors.fragments.SensorsListFragment;
import com.example.sensors.fragments.StepCounterFragment;

/**
 * MainActivity only coordinates navigation.
 *
 * The sensor logic is deliberately moved into fragments so each screen has one
 * clear responsibility. That is the main architecture lesson in this lab.
 */
public class MainActivity extends FragmentActivity {

    private LinearLayout menuContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuContainer = findViewById(R.id.menuContainer);
        buildMenu();

        if (savedInstanceState == null) {
            openFragment(new SensorsListFragment());
        }
    }

    private void buildMenu() {
        addMenuButton("Sensors", new SensorsListFragment());
        addMenuButton("Temperature", SensorGraphFragment.newInstance(
                Sensor.TYPE_AMBIENT_TEMPERATURE,
                "Temperature ambiante",
                SensorGraphFragment.MODE_FIRST_VALUE
        ));
        addMenuButton("Humidite", SensorGraphFragment.newInstance(
                Sensor.TYPE_RELATIVE_HUMIDITY,
                "Humidite relative",
                SensorGraphFragment.MODE_FIRST_VALUE
        ));
        addMenuButton("Proximite", SensorGraphFragment.newInstance(
                Sensor.TYPE_PROXIMITY,
                "Capteur de proximite",
                SensorGraphFragment.MODE_FIRST_VALUE
        ));
        addMenuButton("Magnetic", SensorGraphFragment.newInstance(
                Sensor.TYPE_MAGNETIC_FIELD,
                "Champ magnetique",
                SensorGraphFragment.MODE_MAGNITUDE
        ));
        addMenuButton("Accelerometre", MotionSensorFragment.newInstance(
                Sensor.TYPE_ACCELEROMETER,
                "Accelerometre : x, y, z"
        ));
        addMenuButton("Gravite", MotionSensorFragment.newInstance(
                Sensor.TYPE_GRAVITY,
                "Gravite : x, y, z"
        ));
        addMenuButton("Gyroscope", MotionSensorFragment.newInstance(
                Sensor.TYPE_GYROSCOPE,
                "Gyroscope : rad/s"
        ));
        addMenuButton("Pas", new StepCounterFragment());
        addMenuButton("Boussole", new CompassFragment());
        addMenuButton("Activite", new ActivityRecognitionFragment());
    }

    private void addMenuButton(String title, Fragment fragment) {
        Button button = new Button(this);
        button.setText(title);
        button.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 8, 0);

        menuContainer.addView(button, params);
        button.setOnClickListener(view -> openFragment(fragment));
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
