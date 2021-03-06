package com.example.mci.stepcounter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FilteringStage extends AbstractStage {
    public final static List<Double> COEFF;
    public final static Double COEFF_SUM;

    public final static Integer BELL_SIZE = 6;
    public final static Double BELL_DEVIATION = 0.35d;

    private final static int FRONTIER_MAX = (BELL_SIZE<<1)+1;
    private final List<SensorReading> frontier;

    static {
        Double COEFF_SUM_TMP;
        COEFF = new ArrayList<>();
        COEFF_SUM_TMP = 0.0d;

        // Generate 13 values
        for(int i = 0; i < FRONTIER_MAX; i++){
            // Typical bell curve
            // e^(1/2 * ((i-x)/xy)^2)
            Double exp = Math.pow((i - BELL_SIZE) / (BELL_DEVIATION * BELL_SIZE), 2);
            exp *= -0.5d;
            Double val = Math.pow(Math.E, exp);

            COEFF.add(val);
            COEFF_SUM_TMP += val;
        }
        COEFF_SUM = COEFF_SUM_TMP;
    }

    public FilteringStage(
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
        if(frontier.size() < FRONTIER_MAX) return;

        double total = 0;
        for(int i = 0; i < FRONTIER_MAX; i++)
            total += frontier.get(i).getMagnitude()*COEFF.get(i);

        SensorReading outputReading = new SensorReading(
                frontier.get(FRONTIER_MAX/2).getTimestamp(),
                total/COEFF_SUM
        );
        frontier.remove(0);

        outputQueue.offer(outputReading, 100, TimeUnit.MICROSECONDS);
    }
}
