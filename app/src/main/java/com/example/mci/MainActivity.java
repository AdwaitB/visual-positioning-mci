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

import java.io.File;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String FILENAME = "data.txt";

    private SensorManager sensorManager;
    private SensorCaptureTask sensorCaptureTask;

    private Button toggleBucketing, dumpData, checkfile;

    private TextView x_acc, y_acc, z_acc;
    private TextView x_gyro, y_gyro, z_gyro;
    private TextView time_acc, time_gyro, time_light;
    private TextView light;
    private TextView status;

    boolean track = false;

    private void initViews(){
        time_acc = (TextView) findViewById(R.id.time_acc);

        x_acc = (TextView) findViewById(R.id.x_acc);
        y_acc = (TextView) findViewById(R.id.y_acc);
        z_acc = (TextView) findViewById(R.id.z_acc);

        time_gyro = (TextView) findViewById(R.id.time_gyro);

        x_gyro = (TextView) findViewById(R.id.x_gyro);
        y_gyro = (TextView) findViewById(R.id.y_gyro);
        z_gyro = (TextView) findViewById(R.id.z_gyro);

        time_light = (TextView) findViewById(R.id.time_light);

        light = (TextView) findViewById(R.id.light);

        status = (TextView) findViewById(R.id.status);
    }

    private void initButtons(){
        toggleBucketing = (Button) findViewById(R.id.toggleBucketing);
        toggleBucketing.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                File file = getFile();
                updateStatusForFile(file, "check");
                track = true;
                updateStatusForFile(file, "track");
            }
        });

        checkfile = (Button) findViewById(R.id.checkfile);
        checkfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                File file = getFile();
                updateStatusForFile(file, "check");
            }
        });

        checkfile.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onLongClick(View v) {
                File file = getFile();
                updateStatusForFile(file, "check");
                updateStatusForFile(file, "delete");
                if(file.exists()) file.delete();
                return true;
            }
        });

        dumpData = (Button) findViewById(R.id.dumpData);
        dumpData.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                track = false;
                File file = getFile();
                sensorCaptureTask.serialize(file);
                updateStatusForFile(file, "dumped");
            }
        });
    }

    private void initSensors(){
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensorCaptureTask = new SensorCaptureTask();

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        );

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL
        );

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    private File getFile(){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(directory, FILENAME);
        return file;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            this.time_acc.setText("Acc Time : " + sensorEvent.timestamp);

            this.x_acc.setText("X acc: " + sensorEvent.values[0]);
            this.y_acc.setText("Y acc: " + sensorEvent.values[1]);
            this.z_acc.setText("Z acc: " + sensorEvent.values[2]);
        }
        else if(sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            this.time_gyro.setText("Gyro Time : " + sensorEvent.timestamp);

            this.x_gyro.setText("X acc: " + sensorEvent.values[0]);
            this.y_gyro.setText("Y acc: " + sensorEvent.values[1]);
            this.z_gyro.setText("Z acc: " + sensorEvent.values[2]);
        }
        else if(sensorEvent.sensor.getType()==Sensor.TYPE_LIGHT){
            this.time_light.setText("Light Time : " + sensorEvent.timestamp);

            this.light.setText("Light: " + sensorEvent.values[0]);
        }

        if(track)
            sensorCaptureTask.captureEntry(sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateStatusForFile(File file, String message){
        status.setText(String.format("%s \n %s %s \n %s", getSystemTime(), message, file.exists() ? "yes" : "no", file.getPath()));
    }
}