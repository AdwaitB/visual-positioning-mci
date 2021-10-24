package com.example.mci.stepcounter;

import com.example.mci.MainActivity;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class Exaggerate implements Runnable {
    private final ArrayBlockingQueue<SensorReading> inputQueue, outputQueue;

    private final ArrayList<SensorReading> frontier;
    public final static Integer NUMBER_OF_SAMPLES = 35;


    public Exaggerate(
            ArrayBlockingQueue<SensorReading> inputQueue,
            ArrayBlockingQueue<SensorReading> outputQueue
    ) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;

        frontier = new ArrayList<>();
    }

    @Override
    public void run() {
        while(MainActivity.stepActive){
            if(inputQueue.isEmpty()) continue;
            try {
                SensorReading inputReading = inputQueue.poll(100, TimeUnit.MICROSECONDS);
                frontier.add(inputReading);
                if(frontier.size() < NUMBER_OF_SAMPLES) continue;

                double score = scorePeak(frontier);

                SensorReading outputReading = new SensorReading(
                        frontier.get(NUMBER_OF_SAMPLES>>1).getTimestamp(),
                        score
                );
                frontier.remove(0);
                outputQueue.offer(outputReading, 100, TimeUnit.MICROSECONDS);

//                MainActivity.debug.setText(
//                        String.format("%s\n%s\n%s",
//                                outputReading.getMagnitude().toString(),
//                                Integer.toString(inputQueue.size()),
//                                Integer.toString(outputQueue.size())
//                        )
//                );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
