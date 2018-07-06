package com.example.anarg.openmap2;

import java.util.ArrayList;

/**
 * Train class which defines the structure of the Train class
 * @author  Anarghya Das
 */
public class Train {
    //private instance variables
    private int trainId;
    private String stationCode, CurrentLoc, trainName, direction,trackName;
    private ArrayList<Signal> signals;
    /**
     * Constructor which initialises the Train object
     * @param id train ID
     * @param tn train name
     * @param trackName track name
     */
    Train(int id, String tn, String trackName) {
        signals=new ArrayList<>();
        trainId = id;
        this.trackName=trackName;
        trainName=tn;
    }

    public void setStationCode(String stc) { stationCode = stc; }

    public void setCurrentLoc(String loc) { CurrentLoc = loc; }
    /**
     * Setter for train direction
     * @param d train direction
     */
    public void setDirection(String d){ direction=d; }
    /**
     * Getter for train ID
     * @return train ID
     */
    public int getTrainId(){ return trainId; }
    /**
     * Getter for Train name
     * @return train name
     */
    public String getTrainName() { return trainName; }
    /**
     * Getter for train direction
     * @return train direction
     */
    public String getDirection() { return direction; }
    /**
     * Getter for Track Name
     * @return track Name
     */
    public String getTrackName(){ return  trackName; }
    /**
     * Setter for signals Array List
     * @param s signal
     */
    public void addSignals(Signal s){
        signals.add(s);
    }
    /**
     * Getter for signals arraylist
     * @return arraylist of signals
     */
    public ArrayList<Signal> getSignals(){
        return signals;
    }
    /**
     * @return String representation of the Train
     */
    @Override
    public String toString() {
        return "Train Number: "+trainId+"\n"+"Train Name: "+trainName+"\n"+"Station Code: "+stationCode+"\n"+"Current Location: "+CurrentLoc+"\n"+"Signals:\n"+signals+"\n";
    }
}
