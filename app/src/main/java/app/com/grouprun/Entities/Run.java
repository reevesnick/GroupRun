package app.com.grouprun.Entities;

import java.util.Timer;

/**
 * Created by newuser on 1/28/16.
 */
public class Run {
    private double distance;
    private Timer time;

    public Run(double distance, Timer time) {
        this.setDistance(distance);
        this.setTime(time);
    }

    public Run(){
        distance  =0.0;
        time = null;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Timer getTime() {
        return time;
    }

    public void setTime(Timer time) {
        this.time = time;
    }
}
