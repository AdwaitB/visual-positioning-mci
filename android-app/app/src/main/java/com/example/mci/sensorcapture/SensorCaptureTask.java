package com.example.mci.sensorcapture;

import android.hardware.SensorEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class SensorCaptureTask {
    private final int bucketSize;
    private static final int MS = 1000000;
    private final long startTime;

    private Map<Long, ReadingBucket> buckets;

    public SensorCaptureTask(int bucketSizeMs, long startTime){
        this.startTime = startTime;
        buckets = new TreeMap<>();
        this.bucketSize = bucketSizeMs*MS;
    }

    public void captureEntry(SensorEvent sensorEvent){
        long bucket = getBucket(sensorEvent.timestamp);

        if(!this.buckets.containsKey(bucket))
            this.buckets.put(bucket, new ReadingBucket());

        this.buckets.get(bucket).add(sensorEvent);
    }

    public void serialize(File file){
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(ReadingBucket.getHeader().getBytes());

            for(Long timestamp : buckets.keySet()){
                StringBuilder entry = new StringBuilder(Long.toString((timestamp-startTime)/MS));

                Map<Integer, Float> bucketValues = buckets.get(timestamp).bucketValues;
                for(int i = 0; i < ReadingBucket.BUCKET_SIZE; i++) {
                    entry.append(',');
                    if(bucketValues.containsKey(i))
                        entry.append(bucketValues.get(i).toString());
                }
                entry.append('\n');

                fileOutputStream.write(entry.toString().getBytes());
            }

            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getBucket(long sensorTime){
        sensorTime /= bucketSize;
        return sensorTime * bucketSize;
    }
}
