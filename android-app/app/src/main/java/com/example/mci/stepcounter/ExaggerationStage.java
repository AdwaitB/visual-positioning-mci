package com.example.mci.stepcounter;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ExaggerationStage extends AbstractStage {
    private final ArrayList<SensorReading> frontier;
    public final static Integer NUMBER_OF_SAMPLES = 35;


    public ExaggerationStage(
            ArrayBlockingQueue<SensorReading> inputQueue,
            ArrayBlockingQueue<SensorReading> outputQueue
    ) {
        super(inputQueue, outputQueue);
        frontier = new ArrayList<>();
    }

    @Override
    void runInterruptStep() throws InterruptedException {
        SensorReading inputReading = inputQueue.poll(100, TimeUnit.MICROSECONDS);
        frontier.add(inputReading);
        if(frontier.size() < NUMBER_OF_SAMPLES) return;

        double score = scorePeak(frontier);

        SensorReading outputReading = new SensorReading(
                frontier.get(NUMBER_OF_SAMPLES>>1).getTimestamp(),
                score
        );
        frontier.remove(0);
        outputQueue.offer(outputReading, 100, TimeUnit.MICROSECONDS);
    }

    private double scorePeak(ArrayList<SensorReading> data) {
        int midpoint = (int) data.size() / 2;
        double diffLeft = 0.0d;
        double diffRight = 0.0d;

        for(int i = 0; i < midpoint; i++)
            diffLeft += data.get(midpoint).getMagnitude() - data.get(i).getMagnitude();

        for(int j = midpoint + 1; j < data.size(); j++)
            diffRight += data.get(midpoint).getMagnitude() - data.get(j).getMagnitude();

        return (diffLeft + diffRight) / (NUMBER_OF_SAMPLES - 1);
    }
}
