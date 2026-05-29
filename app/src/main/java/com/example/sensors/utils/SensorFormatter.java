package com.example.sensors.utils;

import android.hardware.Sensor;

/**
 * Small formatting helper.
 *
 * Keeping this outside the fragment makes the list screen easier to read:
 * the fragment handles UI, this class handles sensor-to-text conversion.
 */
public class SensorFormatter {

    public static String format(Sensor sensor) {
        return "Id : " + sensor.getId() + "\n"
                + "Name : " + sensor.getName() + "\n"
                + "Vendor : " + sensor.getVendor() + "\n"
                + "Version : " + sensor.getVersion() + "\n"
                + "Type : " + sensor.getStringType() + "\n"
                + "Int Type : " + sensor.getType() + "\n"
                + "Resolution : " + sensor.getResolution() + "\n"
                + "Power : " + sensor.getPower() + " mA\n"
                + "Maximum Range : " + sensor.getMaximumRange() + "\n"
                + "Min Delay : " + sensor.getMinDelay() + " us\n";
    }
}
