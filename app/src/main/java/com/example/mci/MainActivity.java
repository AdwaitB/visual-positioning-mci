package com.example.mci;

import static com.example.mci.utils.MiscUtils.verifyStoragePermissions;
import static com.example.mci.utils.TimeUtils.getSystemTime;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContextWrapper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mci.sensorcapture.SensorCaptureTask;
import com.example.mci.stepcounter.FilteringStage;
import com.example.mci.stepcounter.ExaggerationStage;
import com.example.mci.stepcounter.DetectionStage;
import com.example.mci.stepcounter.SensorReading;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String FILENAME = "data.txt";
    private Integer SENSOR_LEVEL = SensorManager.SENSOR_DELAY_GAME;

    private SensorManager sensorManager;
    private static final HashMap<Integer, SensorCaptureTask> sensorCaptureTasks;

    private static final ArrayList<Integer> samplingSizes;

    private Button toggleBucketing, dumpData, checkfile;
    private static Button counter;

    private static final ArrayBlockingQueue<SensorReading>
            raw = new ArrayBlockingQueue<>(Short.MAX_VALUE),
            filtered = new ArrayBlockingQueue<>(Short.MAX_VALUE),
            exaggerated = new ArrayBlockingQueue<>(Short.MAX_VALUE),
            detection = new ArrayBlockingQueue<>(Short.MAX_VALUE);

    private static final FilteringStage filteringStage = new FilteringStage(raw, filtered);
    private static final ExaggerationStage exaggerationStage = new ExaggerationStage(filtered, exaggerated);
    private static final DetectionStage detectionStage = new DetectionStage(exaggerated);

    private static Thread filterThread;
    private static Thread exaggerateThread;
    private static Thread detectThread;

    private TextView x_acc, y_acc, z_acc;
    private TextView x_mag, y_mag, z_mag;
    private TextView x_gyro, y_gyro, z_gyro;
    private TextView time_acc, time_mag, time_gyro, time_light;
    private TextView light;
    private TextView status;

    // public
    public static TextView debug;
    public static Integer stepCount = 0;
    public static Boolean stepActive = false;

    boolean track = false;

    static{
        samplingSizes = new ArrayList<>();
        samplingSizes.add(1);
        samplingSizes.add(5);
        samplingSizes.add(10);
        samplingSizes.add(20);
        samplingSizes.add(50);

        sensorCaptureTasks = new HashMap<>();
        for(int samplingSize : samplingSizes)
            sensorCaptureTasks.put(samplingSize, new SensorCaptureTask(samplingSize));
    }

    private void initViews(){
        time_acc = (TextView) findViewById(R.id.time_acc);

        x_acc = (TextView) findViewById(R.id.x_acc);
        y_acc = (TextView) findViewById(R.id.y_acc);
        z_acc = (TextView) findViewById(R.id.z_acc);

        time_mag = (TextView) findViewById(R.id.time_mag);

        x_mag = (TextView) findViewById(R.id.x_mag);
        y_mag = (TextView) findViewById(R.id.y_mag);
        z_mag = (TextView) findViewById(R.id.z_mag);

        time_gyro = (TextView) findViewById(R.id.time_gyro);

        x_gyro = (TextView) findViewById(R.id.x_gyro);
        y_gyro = (TextView) findViewById(R.id.y_gyro);
        z_gyro = (TextView) findViewById(R.id.z_gyro);

        time_light = (TextView) findViewById(R.id.time_light);

        light = (TextView) findViewById(R.id.light);

        status = (TextView) findViewById(R.id.status);

        debug = (TextView) findViewById(R.id.debug);
    }

    private void initButtons(){
        toggleBucketing = (Button) findViewById(R.id.toggleBucketing);
        toggleBucketing.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                track = true;
                for(Integer samplingSize : samplingSizes){
                    File file = getFile(samplingSize);
                    updateStatusForFile(file, "track");
                }
            }
        });

        checkfile = (Button) findViewById(R.id.checkfile);
        checkfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                for(Integer samplingSize : samplingSizes){
                    File file = getFile(samplingSize);
                    updateStatusForFile(file, "check");
                }
            }
        });

        checkfile.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onLongClick(View v) {
                for(Integer samplingSize : samplingSizes){
                    File file = getFile(samplingSize);
                    updateStatusForFile(file, "delete");
                    if(file.exists()) file.delete();
                }
                return true;
            }
        });

        dumpData = (Button) findViewById(R.id.dumpData);
        dumpData.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                track = false;
                for(Integer samplingSize : samplingSizes){
                    File file = getFile(samplingSize);
                    sensorCaptureTasks.get(samplingSize).serialize(file);
                    updateStatusForFile(file, "dumped");
                }
            }
        });

        counter = (Button) findViewById(R.id.counter);
        counter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                stepCount = 0;
                counter.setText(stepCount.toString());
            }
        });

        counter.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                stepActive = !stepActive;
                if(!stepActive) {
                    counter.setText("INACTIVE");
                    joinThreads();
                }
                else {
                    counter.setText(stepCount.toString());
                    startThreads();
                }
                return true;
            }
        });
    }

    private void initSensors(){
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SENSOR_LEVEL
        );

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SENSOR_LEVEL
        );

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SENSOR_LEVEL
        );

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SENSOR_LEVEL
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        verifyStoragePermissions(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initButtons();
        initSensors();
    }

    private File getFile(int prefix){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(directory, prefix + "_" + FILENAME);
        return file;
    }

    private void startThreads(){
        filterThread = new Thread(filteringStage);
        filterThread.start();
        exaggerateThread = new Thread(exaggerationStage);
        exaggerateThread.start();
        detectThread = new Thread(detectionStage);
        detectThread.start();
    }

    private void joinThreads(){
        try {
            filterThread.join();
            exaggerateThread.join();
            detectThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            this.time_acc.setText("Acc Time : " + sensorEvent.timestamp);

            this.x_acc.setText(formatSensorString("X", sensorEvent.values[0]));
            this.y_acc.setText(formatSensorString("Y", sensorEvent.values[0]));
            this.z_acc.setText(formatSensorString("Z", sensorEvent.values[0]));

            if(stepActive) {
                try {
                    SensorReading sensorReading = new SensorReading(
                            sensorEvent.timestamp,
                            Math.sqrt(0
                                    + Math.pow(sensorEvent.values[0], 2)
                                    + Math.pow(sensorEvent.values[1], 2)
                                    + Math.pow(sensorEvent.values[2], 2)
                            )
                    );

                    raw.offer(sensorReading, 100, TimeUnit.MICROSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(sensorEvent.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
            this.time_mag.setText("Gyro Time : " + sensorEvent.timestamp);

            this.x_mag.setText(formatSensorString("X", sensorEvent.values[0]));
            this.y_mag.setText(formatSensorString("Y", sensorEvent.values[1]));
            this.z_mag.setText(formatSensorString("Z", sensorEvent.values[2]));
        }
        else if(sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            this.time_gyro.setText("Gyro Time : " + sensorEvent.timestamp);

            this.x_gyro.setText(formatSensorString("X", sensorEvent.values[0]));
            this.y_gyro.setText(formatSensorString("Y", sensorEvent.values[1]));
            this.z_gyro.setText(formatSensorString("Z", sensorEvent.values[2]));
        }
        else if(sensorEvent.sensor.getType()==Sensor.TYPE_LIGHT){
            this.time_light.setText("Light Time : " + sensorEvent.timestamp);

            this.light.setText(formatSensorString("Light", sensorEvent.values[0]));
        }

        if(track){
            for(SensorCaptureTask sensorCaptureTask : this.sensorCaptureTasks.values())
                sensorCaptureTask.captureEntry(sensorEvent);
        }
    }

    private String formatSensorString(String prefix, float value){
        return prefix + String.format(": %3.5f", value);
    }

    public static void updateStepButton(){
        if(!stepActive)
            counter.setText("INACTIVE");
        else
            counter.setText(stepCount.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateStatusForFile(File file, String message){
        status.setText(String.format("%s \n %s %s \n %s", getSystemTime(), message, file.exists() ? "yes" : "no", file.getPath()));
    }
}