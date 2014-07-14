package org.monroe.team.toolsbox.transport.timer;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.*;

public class TimerAcceptanceTest {

    private int index = 0;
    private final Object mainThreadStop = new Object();
    private final Timer timer = new Timer(true);

    @Ignore
    @Test public void testScheduleExecution(){
        doSchedule();
        synchronized (mainThreadStop){
            try {
                mainThreadStop.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void doSchedule() {
        timer.schedule(createExecution(mainThreadStop), 2000);
    }

    private TimerTask createExecution(final Object mainThreadStop) {
        return new TimerTask() {
            @Override
            public void run() {
                System.out.println(" [start] Execution " + (++index) + " at " + new Date().toString());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(" [stop ] Execution " + index + " at " + new Date().toString());
                if (index > 3){
                    synchronized (mainThreadStop){
                        mainThreadStop.notify();
                    }
                }
                doSchedule();
            }
        };
    }

}