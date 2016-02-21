package app.com.grouprun.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by newuser on 1/28/16.
 */
public class Group {
    private String name;
    private Run run;
    private double longitude;
    private double latitude;
    private List<User> runners;

    public Group(){
        name ="";
        run = new Run();
        runners = new ArrayList<>();
        latitude=0.0;
        longitude=0.0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public List<User> getRunners() {
        return runners;
    }

    public void setRunners(List<User> runners) {
        this.runners = runners;
    }
}
