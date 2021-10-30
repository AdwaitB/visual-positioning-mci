package com.example.mci.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;

import androidx.annotation.RequiresApi;

import com.example.mci.MainActivity;
import com.example.mci.sensorcapture.ReadingBucket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class WifiCapture implements Runnable {
    public static boolean active = false;
    public static Context applicationContext;
    public static File wifiFile;
    private static final int MS = 1000000;

    private long sysTime = 0;

    public WifiCapture(long sysTime) {
        this.sysTime = sysTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void run() {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(wifiFile);
            fileOutputStream.write("Timestamp, wifi_level\n".getBytes());
            WifiManager wifiManager = (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);

            while(active){
                int rssi = wifiManager.getConnectionInfo().getRssi();
                int level = WifiManager.calculateSignalLevel(rssi, 10);
                fileOutputStream.write((
                        Long.toString((SystemClock.elapsedRealtimeNanos() - sysTime)/MS)
                        + ", " + Integer.toString(level)+ "\n"
                ).getBytes());
                Thread.sleep(1);
            }
        } catch (Exception e) {
            MainActivity.appendLog(e.getMessage());
        }

        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
