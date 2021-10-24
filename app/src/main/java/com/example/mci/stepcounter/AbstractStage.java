package com.example.mci.stepcounter;

import com.example.mci.MainActivity;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class AbstractStage implements Runnable {
    protected final ArrayBlockingQueue<SensorReading> inputQueue, outputQueue;

    public static boolean active = false;
    public static int stepCount = 0;

    public AbstractStage(ArrayBlockingQueue<SensorReading> inputQueue, ArrayBlockingQueue<SensorReading> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        while(active){
            if(inputQueue.isEmpty()) continue;
            try {
                runInterruptStep();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void flipActive(){
        active = !active;
    }

    abstract void runInterruptStep() throws InterruptedException;
}
