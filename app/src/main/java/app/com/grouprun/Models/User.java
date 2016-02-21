package app.com.grouprun.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by newuser on 1/28/16.
 */
public class User {
    private String emailAddress;
    private String firstName;
    private String lastName;
    private double weight;
    private double height;
    private boolean isRunning;
    private double latitude;
    private double longitude;
    List<Run> listOfRuns;

    public User(){
        this.emailAddress ="";
        this.firstName = "";
        this.lastName = "";
        this.weight = 0.0;
        this.height = 0.0;
        listOfRuns = new ArrayList<Run>();
    }

    public User(String firstName, String lastName){
        this.firstName = firstName;
        this.lastName = lastName;
        listOfRuns = new ArrayList<Run>();
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

    public boolean isRunning() {
        return isRunning;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
