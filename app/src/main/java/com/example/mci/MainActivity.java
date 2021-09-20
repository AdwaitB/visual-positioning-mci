package com.example.mci;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    Button makefile, checkfile, deletefile;
    TextView x, y, z, status;

    String filename = "data.txt";

    private ContextWrapper cw;
    private File directory, file;
    private FileOutputStream fileOutputStream;
    boolean write = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        verifyStoragePermissions(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        x = (TextView) findViewById(R.id.x);
        y = (TextView) findViewById(R.id.y);
        z = (TextView) findViewById(R.id.z);
        status = (TextView) findViewById(R.id.status);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        );

        makefile = (Button) findViewById(R.id.makefile);
        checkfile = (Button) findViewById(R.id.checkfile);
        deletefile = (Button) findViewById(R.id.deletefile);

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

    public static void verifyStoragePermissions(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    1
            );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            this.x.setText("X: " + sensorEvent.values[0]);
            this.y.setText("Y: " + sensorEvent.values[1]);
            this.z.setText("Z: " + sensorEvent.values[2]);

            if(this.write) {
                String outwrite = getTime() + ", " + getSensorTime(sensorEvent.timestamp) + ", "
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    private String getSensorTime(long sensorTime){
        sensorTime /= 1000000000;
        long sec = sensorTime % 60;
        long min = (sensorTime /60) % 60;
        long hour = (sensorTime /(60*60)) % 24;
        long day = (sensorTime / (24*60*60)) % 24;

        return Long.toString(day) + "-" + Long.toString(hour)
                + "-" + Long.toString(min) + "-" + Long.toString(sec);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTime(){
        LocalDateTime localDateTime = LocalDateTime.now();
        int year = localDateTime.getYear(),
                month = localDateTime.getMonth().getValue(),
                day = localDateTime.getDayOfMonth(),
                hour = localDateTime.getHour(),
                minute = localDateTime.getMinute(),
                second = localDateTime.getSecond(),
                nano = localDateTime.getNano();
        return year + "-" + month + "-" + day + "-"
                + hour + "-" + minute + "-" + second + "-"
                + nano;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateStatusForFile(File file, String message){
        status.setText(String.format("%s \n %s %s \n %s", getTime(), message, file.exists() ? "yes" : "no", file.getPath()));
    }
}