package Entities;

import java.util.List;

/**
 * Created by newuser on 1/28/16.
 */
public class Group {
    private String name;
    private Run run;
    private List<Runner> runners;

    public Group(){
        name ="";
        run = new Run();
        runners = null;
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

    public List<Runner> getRunners() {
        return runners;
    }

    public void setRunners(List<Runner> runners) {
        this.runners = runners;
    }
}
