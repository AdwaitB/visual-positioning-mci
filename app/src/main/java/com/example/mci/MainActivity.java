package com.example.mci;

import static com.example.mci.utils.MiscUtils.verifyStoragePermissions;
import static com.example.mci.utils.TimeUtils.getSensorTime;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;

    Button makefile, checkfile, deletefile;

    TextView x_acc, y_acc, z_acc;
    TextView x_gyro, y_gyro, z_gyro;
    TextView time_acc, time_gyro, time_light;
    TextView light;
    TextView status;

    String filename = "data.txt";

    private ContextWrapper cw;
    private File directory, file;
    private FileOutputStream fileOutputStream;
    boolean write = false;

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
        makefile = (Button) findViewById(R.id.makefile);
        makefile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                try{
                    cw = new ContextWrapper(getApplicationContext());
                    directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    file = new File(directory, filename);

                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write("System Timestamp, Sensor Uptime, x, y, z\n".getBytes());
                    write = true;
                    updateStatusForFile(file, "create");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        checkfile = (Button) findViewById(R.id.checkfile);
        checkfile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                cw = new ContextWrapper(getApplicationContext());
                directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                file = new File(directory, filename);
                updateStatusForFile(file, "check");
            }
        });

        deletefile = (Button) findViewById(R.id.deletefile);
        deletefile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                write = false;
                cw = new ContextWrapper(getApplicationContext());
                directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                file = new File(directory, filename);
                updateStatusForFile(file, "delete");
                if(file.exists()) file.delete();
            }
        });
    }

    private void initSensors(){
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

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

            if(this.write) {
                String outwrite = getSystemTime() + ", " + getSensorTime(sensorEvent.timestamp) + ", "
                        + sensorEvent.values[0] + ", "
                        + sensorEvent.values[1] + ", "
                        + sensorEvent.values[2] + "\n";
                try {
                    fileOutputStream.write(outwrite.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateStatusForFile(File file, String message){
        status.setText(String.format("%s \n %s %s \n %s", getSystemTime(), message, file.exists() ? "yes" : "no", file.getPath()));
    }
}