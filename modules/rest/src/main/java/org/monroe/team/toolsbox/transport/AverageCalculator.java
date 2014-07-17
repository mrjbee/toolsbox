package org.monroe.team.toolsbox.transport;

import java.util.ArrayList;
import java.util.List;

public class AverageCalculator {

    private final int bufferSize;
    private final int recalculationMinChangesSize;
    private final List<Double> valueList;
    private int madeChanges = 0;
    private Double lastCalculatedValue = null;

    public AverageCalculator(int bufferSize) {
        this.bufferSize = bufferSize;
        this.valueList = new ArrayList<Double>(bufferSize);
        recalculationMinChangesSize = (bufferSize/4==0)?1:bufferSize/4;
    }

    public synchronized long getRound() {
        return (long) get();
    }

    public synchronized double get() {
        if (valueList.isEmpty()) return 0;
        if (recalculationMinChangesSize<madeChanges || lastCalculatedValue == null) {
            double averageValue = 0;
            for (Double value : valueList) {
                averageValue += value;
            }
            lastCalculatedValue = averageValue/ valueList.size();
        }
        return lastCalculatedValue;
    }

    public synchronized double putAndGet(double value) {
        put(value);
        return get();
    }

        public synchronized void put(double value) {
        valueList.add(value);
        madeChanges++;
        if (valueList.size() > bufferSize){
            valueList.remove((int)0);
        }
    }
}
