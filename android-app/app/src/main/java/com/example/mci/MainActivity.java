package com.example.mci;

import static com.example.mci.utils.MiscUtils.verifyPermissions;
import static com.example.mci.utils.TimeUtils.getSystemTime;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContextWrapper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mci.sensorcapture.SensorCaptureTask;
import com.example.mci.stepcounter.AbstractStage;
import com.example.mci.stepcounter.FilteringStage;
import com.example.mci.stepcounter.ExaggerationStage;
import com.example.mci.stepcounter.DetectionStage;
import com.example.mci.stepcounter.SensorReading;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String DATA_FILE_SUFFIX = "data.txt";
    private static final String AUDIO_FILENAME = "sound.3gp";
    private Integer SENSOR_LEVEL = SensorManager.SENSOR_DELAY_GAME;

    private SensorManager sensorManager;
    private MediaRecorder mediaRecorder;
    private static final HashMap<Integer, SensorCaptureTask> sensorCaptureTasks;

    private static final ArrayList<Integer> samplingSizes;

    private static final Long SYS_TIME = SystemClock.elapsedRealtimeNanos();

    private static Button counter;

    private static final ArrayBlockingQueue<SensorReading>
            raw = new ArrayBlockingQueue<>(Short.MAX_VALUE),
            filtered = new ArrayBlockingQueue<>(Short.MAX_VALUE),
            exaggerated = new ArrayBlockingQueue<>(Short.MAX_VALUE);

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
    private TextView status, sys_time;

    // public
    public static TextView debug;

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
            sensorCaptureTasks.put(samplingSize, new SensorCaptureTask(samplingSize, SYS_TIME));
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
        sys_time = (TextView) findViewById(R.id.sys_time);
        sys_time.setText(SYS_TIME.toString());

        debug = (TextView) findViewById(R.id.debug);
    }

    private void initButtons(){
        Button toggleBucketing = (Button) findViewById(R.id.toggleBucketing);
        toggleBucketing.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(!track) {
                    for (Integer samplingSize : samplingSizes) {
                        File file = getDataFile(samplingSize);
                        updateStatusForFile(file, "track");
                    }

                    track = true;
                    startAudioRecording();

                    toggleBucketing.setText("STOP RECORDING");
                } else {
                    track = false;
                    stopAudioRecording();

                    for(Integer samplingSize : samplingSizes){
                        File file = getDataFile(samplingSize);
                        sensorCaptureTasks.get(samplingSize).serialize(file);
                        updateStatusForFile(file, "dumped");
                    }

                    toggleBucketing.setText("START RECORDING");
                }
            }
        });

        Button checkfile = (Button) findViewById(R.id.checkfile);
        checkfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                for(Integer samplingSize : samplingSizes){
                    File file = getDataFile(samplingSize);
                    updateStatusForFile(file, "check");
                }
            }
        });

        checkfile.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onLongClick(View v) {
                for(Integer samplingSize : samplingSizes){
                    File file = getDataFile(samplingSize);
                    updateStatusForFile(file, "delete");
                    if(file.exists()) file.delete();
                }
                return true;
            }
        });

        counter = (Button) findViewById(R.id.counter);
        updateStepButton();
        counter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AbstractStage.stepCount = 0;
                updateStepButton();
            }
        });

        Button counterToggle = (Button) findViewById(R.id.counterToggle);
        counterToggle.setText("START\nCOUNTER");
        counterToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AbstractStage.flipActive();
                if(!AbstractStage.active){
                    joinThreads();
                    counterToggle.setText("START\nCOUNTER");
                }
                else{
                    startThreads();
                    counterToggle.setText("STOP\nCOUNTER");
                }
            }
        });
    }

    public static void updateStepButton(){
        counter.setText("STEPS: " + AbstractStage.stepCount + "\n(Click to reset)");
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
        verifyPermissions(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initButtons();
        initSensors();
    }

    private File getDataFile(int prefix){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(directory, prefix + "_" + DATA_FILE_SUFFIX);
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

    private void startAudioRecording(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        mediaRecorder.setOutputFile(directory.getPath() + "/" + AUDIO_FILENAME);

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            appendLog(e.getMessage());
            appendLog(Arrays.toString(e.getStackTrace()));
        }

        mediaRecorder.start();
    }

    private void stopAudioRecording(){
        mediaRecorder.stop();
        mediaRecorder.release();
    }

    private void appendLog(String log){
        debug.append("\n-----------------\n");
        debug.append(log);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            this.time_acc.setText("Acc Time : " + Long.toString(sensorEvent.timestamp-SYS_TIME));

            this.x_acc.setText(formatSensorString("X", sensorEvent.values[0]));
            this.y_acc.setText(formatSensorString("Y", sensorEvent.values[0]));
            this.z_acc.setText(formatSensorString("Z", sensorEvent.values[0]));

            if(AbstractStage.active) {
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
            this.time_mag.setText("Mag Time : " + Long.toString(sensorEvent.timestamp-SYS_TIME));

            this.x_mag.setText(formatSensorString("X", sensorEvent.values[0]));
            this.y_mag.setText(formatSensorString("Y", sensorEvent.values[1]));
            this.z_mag.setText(formatSensorString("Z", sensorEvent.values[2]));
        }
        else if(sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            this.time_gyro.setText("Gyro Time : " + Long.toString(sensorEvent.timestamp-SYS_TIME));

            this.x_gyro.setText(formatSensorString("X", sensorEvent.values[0]));
            this.y_gyro.setText(formatSensorString("Y", sensorEvent.values[1]));
            this.z_gyro.setText(formatSensorString("Z", sensorEvent.values[2]));
        }
        else if(sensorEvent.sensor.getType()==Sensor.TYPE_LIGHT){
            this.time_light.setText("Light Time : " + Long.toString(sensorEvent.timestamp-SYS_TIME));

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateStatusForFile(File file, String message){
        status.setText(String.format("%s \n %s %s \n %s", getSystemTime(), message, file.exists() ? "yes" : "no", file.getPath()));
    }
}