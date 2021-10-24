package com.example.mci.stepcounter;

import com.example.mci.MainActivity;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Detect implements Runnable {
    private final static long STEP_DURATION_THRESHOLD = (long) 2e8;
    private final static float MAGNITUDE_THRESHOLD = 1.2f;

    private final double STD_MIN = 0.5f;
    private final double STD_MAX = 1.5f;

    private final ArrayBlockingQueue<SensorReading> inputQueue;

    private double mean;
    private double std;
    private int count;

    private SensorReading lastPeakReading;

    public Detect(
            ArrayBlockingQueue<SensorReading> inputQueue
    ) {
        this.inputQueue = inputQueue;
        lastPeakReading = null;
        mean = 0.0d;
        std = (STD_MIN + STD_MAX)/2;
        count = 0;
    }

    @Override
    public void run() {
        while(MainActivity.stepActive){
            if(inputQueue.isEmpty()) continue;
            try {
                SensorReading inputReading = inputQueue.poll(100, TimeUnit.MICROSECONDS);
                count++;
                double oMean = mean;

                switch(count) {
                    case 1:
                        mean = inputReading.getMagnitude();
                        std = 0f;
                        break;
                    case 2:
                        mean = (mean + inputReading.getMagnitude()) / 2;
                        std = (float) Math.sqrt(Math.pow(inputReading.getMagnitude() - mean, 2) +
                                Math.pow(oMean - mean,2)) / 2;
                        break;
                    default:
                        mean = (inputReading.getMagnitude() + (count-1) * mean) / count;

                        // online standard variance unbiased : Welford's online algorithm
                        std = (float) Math.sqrt(
                                ((count-2) * Math.pow(std, 2) / (count-1)) +

                                // add unbiased factor to fix the numerical instability
                                Math.pow(oMean - mean, 2) +
                                Math.pow(inputReading.getMagnitude() - mean, 2) / count
                        );
                        break;
                }

                if(std > STD_MAX) std = STD_MAX;
                if(std < STD_MIN) std = STD_MIN;

                if(count > 15) {
                    if((inputReading.getMagnitude() - mean) > std * MAGNITUDE_THRESHOLD) {
                        if(lastPeakReading == null) {
                            lastPeakReading = inputReading;
                            MainActivity.stepCount++;
                        } else if (inputReading.getTimestamp() - lastPeakReading.getTimestamp() > STEP_DURATION_THRESHOLD) {
                            lastPeakReading = inputReading;
                            MainActivity.stepCount++;
                        } else if(inputReading.getMagnitude() > lastPeakReading.getMagnitude())
                            lastPeakReading = inputReading;
                    }
                }

                MainActivity.debug.setText(
                        String.format("Mean: %f\nDeviation (unbiased): %f\n", mean, std)
                );
                MainActivity.updateStepButton();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
