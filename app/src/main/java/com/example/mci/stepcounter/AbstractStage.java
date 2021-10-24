package com.example.mci.stepcounter;

import com.example.mci.MainActivity;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class AbstractStage implements Runnable {
    protected final ArrayBlockingQueue<SensorReading> inputQueue, outputQueue;

    public AbstractStage(ArrayBlockingQueue<SensorReading> inputQueue, ArrayBlockingQueue<SensorReading> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        while(MainActivity.stepActive){
            if(inputQueue.isEmpty()) continue;
            try {
                runInterruptStep();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    abstract void runInterruptStep() throws InterruptedException;
}
