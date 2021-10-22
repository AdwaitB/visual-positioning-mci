package com.example.mci.sensorcapture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstraction to insert multiple values in a bucket for a fixed time duration.
 */
public class ReadingBucket {
    public static int BUCKET_SIZE = 0;

    private static final Map<Integer, Integer> SENSOR_READING_SIZES;
    private static final Map<Integer, Integer> SENSOR_INDEX_START;
    private static final Map<Integer, String> SENSOR_INDEX_TO_NAME;

    static {
        SENSOR_READING_SIZES = new TreeMap<>();
        SENSOR_INDEX_START = new TreeMap<>();
        SENSOR_INDEX_TO_NAME = new TreeMap<>();

        SENSOR_READING_SIZES.put(Sensor.TYPE_ACCELEROMETER, 3);
        SENSOR_READING_SIZES.put(Sensor.TYPE_MAGNETIC_FIELD, 3);
        SENSOR_READING_SIZES.put(Sensor.TYPE_GYROSCOPE, 2);
        SENSOR_READING_SIZES.put(Sensor.TYPE_LIGHT, 1);

        for(Integer key : SENSOR_READING_SIZES.keySet()) {
            SENSOR_INDEX_START.put(key, BUCKET_SIZE);
            BUCKET_SIZE += SENSOR_READING_SIZES.get(key);
        }

        SENSOR_INDEX_TO_NAME.put(0, "acc_x");
        SENSOR_INDEX_TO_NAME.put(1, "acc_y");
        SENSOR_INDEX_TO_NAME.put(2, "acc_z");
        SENSOR_INDEX_TO_NAME.put(5, "mag_x");
        SENSOR_INDEX_TO_NAME.put(6, "mag_y");
        SENSOR_INDEX_TO_NAME.put(7, "mag_z");
        SENSOR_INDEX_TO_NAME.put(3, "gyro_x");
        SENSOR_INDEX_TO_NAME.put(4, "gyro_y");
        SENSOR_INDEX_TO_NAME.put(8, "light");
    }

    public static String getHeader(){
        StringBuilder builder = new StringBuilder();

        builder.append("timestamp");

        for(String val : SENSOR_INDEX_TO_NAME.values())
            builder.append(',').append(val);

        builder.append('\n');

        return builder.toString();
    }

    Map<Integer, Float> bucketValues;
    Map<Integer, Integer> bucketCounts;

    ReadingBucket(){
        bucketValues = new HashMap<>();
        bucketCounts = new HashMap<>();
    }

    public void add(SensorEvent sensorEvent){
        Integer sensorIndexStart = SENSOR_INDEX_START.get(sensorEvent.sensor.getType());
        Integer sensorValuesQuantity = SENSOR_READING_SIZES.get(sensorEvent.sensor.getType());

        for(Integer index = sensorIndexStart; index < sensorIndexStart + sensorValuesQuantity; index++){
            Float value = sensorEvent.values[index - sensorIndexStart];

            if(!bucketValues.containsKey(index)) {
                bucketValues.put(index, value);
                bucketCounts.put(index, 1);
            }
            else {
                Float avg = bucketValues.get(index);
                Integer cnt = bucketCounts.get(index);

                avg *= cnt;
                avg += value;

                bucketValues.put(index, avg);
                bucketCounts.put(index, cnt+1);
            }
        }
    }

    public Map<Integer, Float> consolidate(){
        return new HashMap<>(this.bucketValues);
    }
}
