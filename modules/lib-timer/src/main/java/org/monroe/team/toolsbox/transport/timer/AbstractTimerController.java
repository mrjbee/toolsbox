package org.monroe.team.toolsbox.transport.timer;

import org.apache.logging.log4j.Logger;
import org.monroe.team.toolsbox.logging.Logs;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AbstractTimerController {

    private List<Schedule> scheduleList = new ArrayList<Schedule>(5);
    private Logger log = Logs.core;

    @PostConstruct
    final public void initTimers(){
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            if (AnnotationUtils.findAnnotation(method,TimerSchedule.class)!= null){
               TimerSchedule schedule = AnnotationUtils.findAnnotation(method, TimerSchedule.class);
               scheduleList.add(new Schedule(schedule, method));
            }
        }

        for (Schedule schedule : scheduleList) {
            schedule.start();
        }
    }

    @PreDestroy
    final public void destroyTimers(){
        for (Schedule schedule : scheduleList) {
            schedule.stop();
        }
    }

    protected void onException(String label, Exception e){
        log.warn("Exception during timer [{}] execution",label, e);
    }

    protected void onScheduleException(String label, Exception e) {
        log.error("Exception during timer [{}] schedule execution", label, e);
    }

    private class Schedule{

        private final TimerSchedule timerScheduleDetails;
        private final Method executionMethod;
        private final Timer timer;

        private Schedule(TimerSchedule timerScheduleDetails, Method executeMethod) {
            this.timerScheduleDetails = timerScheduleDetails;
            this.executionMethod = executeMethod;
            timer = new Timer(getLabel(),true);
        }

        public void start() {
            doScheduling(getStartupRate());
        }


        private long getStartupRate() {
            return timerScheduleDetails.startDelay();
        }

        private long getFixedRate() {
            return timerScheduleDetails.value();
        }

        private void doScheduling(long ms) {
            if (ms == -1) return;
            try{
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runImpl();
                    }
                },ms);
            } catch (Exception e){
                AbstractTimerController.this.onScheduleException(getLabel(),e);
            }
        }

        public void stop() {
            try{
                timer.cancel();
            }catch (Exception e){
                AbstractTimerController.this.onScheduleException(getLabel(),e);
            }
        }

        public void runImpl() {
            AbstractTimerController obj = AbstractTimerController.this;
            try {
                executionMethod.invoke(obj);
            } catch (Exception e) {
               obj.onException(getLabel(),e);
            }
            doScheduling(getFixedRate());
        }

        public String getLabel() {
            return AbstractTimerController.this.getClass().getName()+":"+executionMethod.getName();
        }

    }


}
