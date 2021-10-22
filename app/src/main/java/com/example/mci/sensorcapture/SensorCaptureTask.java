package com.example.mci.sensorcapture;

import android.hardware.SensorEvent;

import java.util.HashMap;
import java.util.Map;

public class SensorCaptureTask {
    private static final int BUCKET_WINDOW = 10;

    private Map<Long, ReadingBucket> buckets;

    SensorCaptureTask(){
        buckets = new HashMap<>();
    }

    public void captureEntry(SensorEvent sensorEvent){
        long bucket = getBucket(sensorEvent.timestamp);

        if(!this.buckets.containsKey(bucket))
            this.buckets.put(bucket, new ReadingBucket());

        this.buckets.get(bucket).add(sensorEvent);
    }

    public void serialize(){

    }

    private long getBucket(long sensorTime){
        sensorTime /= BUCKET_WINDOW;
        return sensorTime * BUCKET_WINDOW;
    }
}
