package Entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by newuser on 1/28/16.
 */
public class Runner {
    private String firstName;
    private String lastName;
    private double weight;
    private double height;
    List<Run> activities;

    public Runner(){
        this.firstName = "";
        this.lastName = "";
        this.weight = 0.0;
        this.height = 0.0;
        activities = new ArrayList<Run>();
    }

    public Runner(String firstName, String lastName){
        this.firstName = firstName;
        this.lastName = lastName;
        activities = new ArrayList<Run>();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
