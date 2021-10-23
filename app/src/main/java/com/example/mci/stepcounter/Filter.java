package com.example.mci.stepcounter;

import com.example.mci.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class Filter implements Runnable {

    private final ArrayBlockingQueue<SensorReading> inputQueue, outputQueue;

    public final static List<Double> coeff;

    public final static Integer BELL_SIZE = 6;
    public final static Double BELL_DEVIATION = 0.35d;

    static {
        coeff = new ArrayList<>();

        // Generate 13 values
        for(int i = 0; i < ((BELL_SIZE<<1)+1); i++){
            // Typical bell curve
            // e^(1/2 * ((i-x)/xy)^2)
            Double exp = Math.pow((i - BELL_SIZE) / (BELL_DEVIATION * BELL_SIZE), 2);
            exp *= -0.5d;
            coeff.add(Math.pow(Math.E, exp));
        }
    }

    public Filter(
            ArrayBlockingQueue<SensorReading> inputQueue,
            ArrayBlockingQueue<SensorReading> outputQueue
    ) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        while(MainActivity.stepActive){
            if(inputQueue.isEmpty()) continue;
            SensorReading sensorReading = null;
            try {
                sensorReading = inputQueue.poll(100, TimeUnit.MICROSECONDS);

                MainActivity.debug.setText(
                        sensorReading.getMagnitude().toString() + '\n' +
                                Integer.toString(inputQueue.size())
                );

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
