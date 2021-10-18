package com.example.mci.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;

public class TimeUtils {
    public static String getSensorTime(long sensorTime){
        sensorTime /= 1000000000;
        long sec = sensorTime % 60;
        long min = (sensorTime /60) % 60;
        long hour = (sensorTime /(60*60)) % 24;
        long day = (sensorTime / (24*60*60)) % 24;

        return Long.toString(day) + "-" + Long.toString(hour)
                + "-" + Long.toString(min) + "-" + Long.toString(sec);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getSystemTime(){
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
}
