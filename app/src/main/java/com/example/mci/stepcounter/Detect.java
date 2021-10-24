package com.example.mci.stepcounter;

import com.example.mci.MainActivity;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class Detect implements Runnable {
    private final ArrayBlockingQueue<SensorReading> inputQueue, outputQueue;

    private static int number_of_steps;
    private double mean;
    private double std;
    private int count;

    private final float threshold = 1.2f;
    private long lastSeen;

    public Detect(
            ArrayBlockingQueue<SensorReading> inputQueue,
            ArrayBlockingQueue<SensorReading> outputQueue
    ) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        lastSeen = 0;
        number_of_steps = 0;
        mean = 0.0d;
        std = 0.0d;
        count = 0;

    }

    @Override
    public void run() {
        while(MainActivity.stepActive){
            if(inputQueue.isEmpty()) continue;
            try {
                SensorReading inputReading = inputQueue.poll(100, TimeUnit.MICROSECONDS);
                count++;
                double o_mean = mean;
                switch(count) {
                    case 1:
                        mean = inputReading.getMagnitude();
                        std = 0f;
                        break;
                    case 2:
                        mean = (mean + inputReading.getMagnitude()) / 2;
                        std = (float)Math.sqrt(Math.pow(inputReading.getMagnitude() - mean,2) + Math.pow(o_mean - mean,2)) / 2;
                        break;
                    default:
                        mean = (inputReading.getMagnitude() + (count - 1) * mean) / count;
                        std = (float)Math.sqrt(((count - 2) * Math.pow(std,2) / (count - 1)) + Math.pow(o_mean - mean, 2) +  Math.pow(inputReading.getMagnitude() - mean,2) / count);
                }

                if(count > 15) {
                    if((inputReading.getMagnitude() - mean) > std * threshold) {
                        if(lastSeen == 0) {
                            lastSeen = inputReading.getTimestamp();
                            number_of_steps++;
                        } else if(inputReading.getTimestamp() - lastSeen > 300000000) {
                            lastSeen = inputReading.getTimestamp();
                            number_of_steps++;
                        }
                    }
                }

                MainActivity.debug.setText(
                        String.format("Steps: %s",Integer.toString(number_of_steps))
                );

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
